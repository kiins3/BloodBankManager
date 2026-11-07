"""Fill a fresh local database with realistic demo data by walking the real API.

It runs the whole business flow end to end, so it doubles as a smoke test:
campaigns -> walk-in and online donors -> screening -> blood collection ->
lab tests -> component separation -> labelling -> storage ->
hospital requests -> review -> export -> delivery.

Only one step touches the database directly: a scheduled job opens campaigns
once an hour, so the script moves one campaign to "today" and opens it with an
UPDATE through `docker compose exec mysql`.

Run it once on an empty database:
    docker compose down -v && docker compose up -d
    python scripts/seed_demo.py [--api http://localhost:8080]

Accounts: admin@bloodbank.local / Admin@123; doctor@, nurse@, tech@, inventory@,
hospital@ and donor@bloodbank.local / 123456.
"""
import argparse
import json
import subprocess
import sys
import urllib.error
import urllib.request
from datetime import datetime, timedelta

API = "http://localhost:8080"
ADMIN = ("admin@bloodbank.local", "Admin@123")
DEMO_PASSWORD = "123456"  # the backend gives every account it creates this password

STAFF = [  # email, name, role, position, gender, dob
    ("doctor@bloodbank.local", "BS. Nguyễn Thị Hạnh", "STAFF_TECH", "BAC_SI", "Nữ", "1985-04-12"),
    ("nurse@bloodbank.local", "Trần Thu Hà", "STAFF_TECH", "Y_TA", "Nữ", "1994-08-03"),
    ("tech@bloodbank.local", "Lê Văn Đức", "STAFF_TECH", "KY_THUAT", "Nam", "1992-02-20"),
    ("inventory@bloodbank.local", "Phạm Minh Tuấn", "STAFF_INVENTORY", "QUAN_LY_KHO", "Nam", "1990-11-05"),
]
HOSPITALS = [  # email, name, address, hotline
    ("hospital@bloodbank.local", "Bệnh viện Bạch Mai", "78 Giải Phóng, Đống Đa, Hà Nội", "02438693731"),
    ("vietduc@bloodbank.local", "Bệnh viện Việt Đức", "40 Tràng Thi, Hoàn Kiếm, Hà Nội", "02438253531"),
    ("k@bloodbank.local", "Bệnh viện K", "43 Quán Sứ, Hoàn Kiếm, Hà Nội", "02438252143"),
]
DONOR_EMAIL = "donor@bloodbank.local"

failures = []


def call(method, path, token=None, body=None, ok_fail=False):
    data = None if body is None else json.dumps(body).encode()
    req = urllib.request.Request(API + path, data=data, method=method)
    req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", "Bearer " + token)
    try:
        with urllib.request.urlopen(req, timeout=60) as res:
            raw = res.read().decode() or "null"
            status = res.status
    except urllib.error.HTTPError as e:
        raw, status = e.read().decode() or "null", e.code
    try:
        payload = json.loads(raw)
    except json.JSONDecodeError:
        payload = raw
    if status >= 400 and not ok_fail:
        msg = payload.get("message", payload) if isinstance(payload, dict) else payload
        failures.append(f"{method} {path} -> {status}: {msg}")
        print(f"  ! {method} {path} -> {status}: {msg}")
    return status, payload


def login(account):
    _, res = call("POST", "/auth/login", body={"email": account[0], "password": account[1]})
    return res["accessToken"]


def unwrap(res):
    """Most endpoints wrap results as {code, message, data}; some return the list directly."""
    if isinstance(res, dict):
        for key in ("data", "content", "result"):
            if key in res:
                return unwrap(res[key]) if isinstance(res[key], dict) and "content" in res[key] else res[key]
    return res


def sql(statement):
    subprocess.run(
        ["docker", "compose", "exec", "-T", "mysql", "sh", "-c",
         f'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" BloodBankDB -e "{statement}"'],
        check=True, capture_output=True,
    )


def vn_datetime(dt):
    # Request DTOs use @JsonFormat("dd-MM-yyyy HH:mm") / ("dd-MM-yyyy")
    return dt.strftime("%d-%m-%Y %H:%M")


def vn_date(d):
    return d.strftime("%d-%m-%Y")


def main():
    global API
    parser = argparse.ArgumentParser()
    parser.add_argument("--api", default=API)
    API = parser.parse_args().api.rstrip("/")

    admin = login(ADMIN)
    now = datetime.now().replace(minute=0, second=0, microsecond=0)

    print("1. Accounts, hospitals and storage equipment")
    for i, (email, name, role, position, gender, dob) in enumerate(STAFF):
        call("POST", "/api/admin/user/create-staff-account", admin, {
            "fullName": name, "email": email, "dob": dob, "cccd": f"0010850{i + 1:05d}",
            "gender": gender, "phone": f"09880000{i + 1:02d}", "role": role, "position": position}, ok_fail=True)
    for email, name, addr, phone in HOSPITALS:
        call("POST", "/api/admin/user/create-hospital-account", admin, {
            "hospitalName": name, "email": email, "password": DEMO_PASSWORD,
            "address": addr, "hotline": phone, "status": "ACTIVE"}, ok_fail=True)
    call("POST", "/auth/sign-up", body={"email": DONOR_EMAIL, "name": "Nguyễn Minh Anh",
                                        "cccd": "001201012345", "phone": "0912345678"}, ok_fail=True)
    tech = login(("tech@bloodbank.local", DEMO_PASSWORD))
    doctor = login(("doctor@bloodbank.local", DEMO_PASSWORD))
    inv = login(("inventory@bloodbank.local", DEMO_PASSWORD))
    hosp = login((HOSPITALS[0][0], DEMO_PASSWORD))
    donor = login((DONOR_EMAIL, DEMO_PASSWORD))

    _, res = call("GET", "/api/staff/storage-equipment/active", inv)
    if not unwrap(res):
        for name, ptype, cap in [
            ("Tủ máu toàn phần A1", "MAU_TOAN_PHAN", 60), ("Tủ hồng cầu B1", "TUI_HONG_CAU", 80),
            ("Tủ huyết tương đông lạnh C1", "TUI_HUYET_TUONG", 80), ("Tủ lắc tiểu cầu D1", "TUI_TIEU_CAU", 40),
        ]:
            call("POST", "/api/admin/storage-equipment/create-equipment", admin,
                 {"name": name, "productType": ptype, "standard": "ISO 13485", "maxCapacity": cap})
        _, res = call("GET", "/api/staff/storage-equipment/active", inv)
    equipment = {e.get("productType"): e.get("equipmentId") for e in unwrap(res) or []}
    print("   equipment:", equipment)

    print("2. Campaigns")
    campaigns = [
        ("Hiến máu nhân đạo ĐH Giao thông Vận tải", "Số 3 Cầu Giấy, Láng Thượng, Đống Đa, Hà Nội", 4, 120),
        ("Giọt hồng yêu thương – Viện Huyết học", "Số 1 Phạm Văn Bạch, Cầu Giấy, Hà Nội", 6, 200),
        ("Chủ nhật Đỏ 2026", "Cung Văn hóa Hữu nghị, 91 Trần Hưng Đạo, Hà Nội", 9, 300),
        ("Hành trình Đỏ – Hà Đông", "Nhà văn hóa quận Hà Đông, Hà Nội", 13, 150),
    ]
    for name, loc, days, target in campaigns:
        start = now + timedelta(days=days, hours=8 - now.hour)
        call("POST", "/api/admin/event/create-event", admin, {
            "eventName": name, "location": loc, "startDate": vn_datetime(start),
            "endDate": vn_datetime(start + timedelta(hours=9)), "targetAmount": target}, ok_fail=True)
    _, res = call("GET", "/api/shared/event/get-list-event", admin)
    events = {e["eventName"]: e["eventId"] for e in unwrap(res)}
    live = events[campaigns[0][0]]
    print("   events:", events)

    print("3. Staff assignment for today's campaign")
    _, res = call("GET", "/api/admin/staff/get-list-staff?page=0&size=50", admin)
    role_for = {"BAC_SI": "KHAM_SANG_LOC", "Y_TA": "LAY_MAU", "KY_THUAT": "TIEP_DON"}
    assignments = [{"staffId": s["staffId"], "role": role_for[s["position"]]}
                   for s in res.get("content", []) if s.get("position") in role_for]
    call("POST", f"/api/admin/assignment/assignment-staff/{live}", admin,
         {"eventId": live, "assignments": assignments})

    # Move the first campaign to today and open it (the hourly job would open it later).
    sql(f"UPDATE events SET start_date='{now - timedelta(hours=2):%Y-%m-%d %H:%M:%S}', "
        f"end_date='{now + timedelta(hours=8):%Y-%m-%d %H:%M:%S}', status='DANG_MO' WHERE event_id={live}")

    print("4. Online donor registers and checks in with their ticket")
    call("PUT", "/api/donor/update-donor", donor, {
        "fullName": "Nguyễn Minh Anh", "gender": "Nữ", "dob": "14-05-2001",
        "bloodType": "O", "rhFactor": "+", "address": "Cầu Giấy, Hà Nội"})
    call("POST", f"/api/donor/registration/regisEvent/{live}", donor)
    call("POST", f"/api/donor/registration/regisEvent/{events[campaigns[1][0]]}", donor, ok_fail=True)
    _, ticket = call("GET", f"/api/donor/registration/get-my-ticket/{live}", donor)
    ticket = unwrap(ticket) or {}
    code = ticket.get("ticketCode")
    if code:
        call("POST", f"/api/staff/registration/checkin/{code}/{live}", tech)

    print("5. Walk-in donors")
    walk_ins = [
        ("Trần Văn Hùng", "Nam", "1995-03-02", "A", "+"), ("Lê Thị Mai", "Nữ", "1998-11-20", "B", "+"),
        ("Phạm Quốc Bảo", "Nam", "1990-07-09", "O", "+"), ("Hoàng Thu Trang", "Nữ", "2000-01-25", "AB", "+"),
        ("Đỗ Đức Thắng", "Nam", "1993-09-15", "A", "+"), ("Vũ Ngọc Lan", "Nữ", "1997-04-30", "O", "-"),
        ("Bùi Minh Khoa", "Nam", "1989-12-12", "B", "+"), ("Ngô Thảo Vy", "Nữ", "2002-06-18", "A", "-"),
        ("Đặng Hoài Nam", "Nam", "1996-08-08", "O", "+"),
    ]
    for i, (name, gender, dob, btype, rh) in enumerate(walk_ins):
        call("POST", f"/api/staff/donor/regis-for-visitor/{live}", tech, {
            "fullName": name, "email": f"walkin{i + 1}@example.com", "cccd": f"0012030{i + 1:05d}",
            "gender": gender, "dob": dob, "phone": f"09120000{i + 1:02d}", "address": "Hà Nội",
            "bloodType": btype, "rhFactor": rh})

    print("6. Screening and collection")
    _, res = call("GET", f"/api/staff/registration/get-all-donors-of-event/{live}", tech)
    regs = unwrap(res) or []
    for i, r in enumerate(regs):
        regis_id = r.get("registrationId") or r.get("regisId")
        donor_id = r.get("donorId")
        eligible = i != 3  # one donor turned away at screening
        call("PUT", f"/api/staff/registration/screening/{regis_id}", doctor, {
            "donorId": donor_id, "weight": 58 + i * 2, "hemoglobin": 13.2 + (i % 4) * 0.4,
            "bloodPressure": "120/80", "heartRate": 72 + i, "expectedVolume": 350,
            "isEligible": eligible, "rejectionReason": None if eligible else "Huyết áp cao (150/95)"})
        if eligible and i < len(regs) - 1:  # leave the last donor waiting for collection
            call("POST", f"/api/staff/registration/donate-blood/{regis_id}", tech,
                 {"actualVolume": 350, "isSuccess": True})

    print("7. Lab tests, separation, labels, storage")
    # The lab enters the blood group it measured; here it matches what each donor declared.
    blood_of = {name: (btype, rh) for name, _, _, btype, rh in walk_ins}
    blood_of["Nguyễn Minh Anh"] = ("O", "+")
    _, res = call("GET", "/api/staff/bloodbag/get-list-blood-bag?status=CHO_XET_NGHIEM", tech)
    bags = unwrap(res) or []
    for i, b in enumerate(bags):
        bag_id = b.get("bloodBagId")
        _, detail = call("GET", f"/api/staff/bloodbag/get-blood-bag-detail/{bag_id}", tech)
        detail = unwrap(detail) or {}
        btype, rh = blood_of.get(detail.get("donorName"), ("O", "+"))
        hbv = "DUONG_TINH" if i == 2 else "AM_TINH"  # one reactive sample goes to disposal
        call("POST", f"/api/staff/bloodbag/test-result/{bag_id}", tech, {
            "bloodType": btype, "rhFactor": rh, "hiv": "AM_TINH", "hbv": hbv, "hcv": "AM_TINH",
            "syphilis": "AM_TINH", "malaria": "AM_TINH", "isPhysicalError": False})
        if hbv == "AM_TINH" and i < len(bags) - 1:  # keep one whole-blood bag unseparated
            call("POST", f"/api/staff/bloodbag/separate-blood/{bag_id}", tech,
                 {"redCellVolume": 220, "plasmaVolume": 140, "plateletsVolume": 40})

    _, res = call("GET", "/api/staff/bloodbag/get-list-blood-bag?status=CHO_IN_NHAN", inv)
    children = unwrap(res) or []
    for b in children:
        call("POST", f"/api/staff/bloodbag/print-label/{b['bloodBagId']}", inv)
    by_type = {}
    for b in children:
        by_type.setdefault(b.get("productType"), []).append(b["bloodBagId"])
    for ptype, ids in by_type.items():
        if ptype in equipment:
            call("POST", f"/api/staff/bloodbag/store-blood/{equipment[ptype]}", inv, ids)

    print("8. Hospital requests, review, export, delivery")
    soon_d, later_d = (now + timedelta(days=1)).date(), (now + timedelta(days=5)).date()
    soon, later = vn_date(soon_d), vn_date(later_d)
    call("POST", "/api/hospital/blood-request/rq", hosp, {
        "deadlineDate": soon, "priority": "KHAN_CAP", "details": [
            {"bloodType": "A+", "productType": "TUI_HONG_CAU", "volume": 350, "quantity": 2, "deadline": soon_d.isoformat()}]})
    call("POST", "/api/hospital/blood-request/rq", hosp, {
        "deadlineDate": later, "priority": "BINH_THUONG", "details": [
            {"bloodType": "A+", "productType": "TUI_HUYET_TUONG", "volume": 350, "quantity": 1, "deadline": later_d.isoformat()},
            {"bloodType": "B+", "productType": "TUI_TIEU_CAU", "volume": 350, "quantity": 1, "deadline": later_d.isoformat()}]})
    call("POST", "/api/hospital/blood-request/rq", hosp, {
        "deadlineDate": later, "priority": "BINH_THUONG", "details": [
            {"bloodType": "AB-", "productType": "TUI_HONG_CAU", "volume": 350, "quantity": 3, "deadline": later_d.isoformat()}]})

    _, res = call("GET", "/api/staff/blood-request/list-request", inv)
    requests = sorted(unwrap(res) or [], key=lambda r: r.get("requestId", 0))
    request_ids = sorted({r["requestId"] for r in requests})
    if request_ids:
        first = request_ids[0]
        _, detail = call("GET", f"/api/shared/blood-request/detail/{first}", inv)
        details = (detail or {}).get("requestedItems", [])
        call("PATCH", f"/api/staff/blood-request/review-request/{first}", inv, {"approvedDetails": [
            {"detailId": d["detailId"], "approvedQuantity": d["quantity"]} for d in details]})
        _, res = call("GET", f"/api/staff/blood-request/list-suggested-bag/{first}", inv)
        suggested = unwrap(res) or []
        bag_ids = [s.get("bloodBagId") for s in suggested][:sum(d["quantity"] for d in details)]
        for s in suggested[:len(bag_ids)]:
            call("POST", f"/api/staff/blood-request/scan-blood-bag/{first}", inv, {"bagCode": s.get("bagCode")})
        if bag_ids:
            call("POST", f"/api/staff/blood-request/export-blood/{first}", inv, {"bloodBagId": bag_ids})
            call("PATCH", f"/api/hospital/blood-request/track-order/{first}", hosp, {"rejectedBags": []})

    print()
    if failures:
        print(f"Done with {len(failures)} failed call(s):")
        for f in failures:
            print("  -", f)
        sys.exit(1)
    print("Done: demo data ready.")


if __name__ == "__main__":
    main()
