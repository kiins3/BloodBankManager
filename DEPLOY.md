# Deploying for free

| Part | Where | Free tier |
|---|---|---|
| API (Docker) | [Render](https://render.com) web service | 512 MB, sleeps after 15 min idle (first request takes ~1 min) |
| Redis | Render Key Value | 25 MB |
| MySQL | [Aiven](https://aiven.io) free MySQL | 1 GB |
| RabbitMQ | [CloudAMQP](https://www.cloudamqp.com) "Little Lemur" | 1M messages/month |
| Frontend | [Vercel](https://vercel.com) | static hosting |

## 1. MySQL on Aiven

1. Create a free **MySQL** service (pick a region near Singapore).
2. From the service overview, copy host, port, user (`avnadmin`) and password.
3. `DB_URL` is:
   ```
   jdbc:mysql://<host>:<port>/defaultdb?sslMode=REQUIRED&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh
   ```
   Tables are created on first start (`ddl-auto=update`).

## 2. RabbitMQ on CloudAMQP

1. Create a free **Little Lemur** instance (region: AWS ap-southeast-1).
2. Copy the **AMQP URL** (`amqps://user:pass@host/vhost`). This is `RABBITMQ_URL`.

## 3. API and Redis on Render

1. Render dashboard → **New → Blueprint** → pick this repository. Render reads [`render.yaml`](render.yaml) and creates `bloodbank-api` and `bloodbank-redis`.
2. Fill in the values it asks for: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `RABBITMQ_URL`, `ADMIN_PASSWORD`, and optionally `MAIL_USERNAME` / `MAIL_PASSWORD` (Gmail App Password). `JWT_SECRET` is generated for you.
3. Wait for the first build (~5–8 min). Check `https://<service>.onrender.com/actuator/health` returns `{"status":"UP"}` and `/swagger-ui.html` opens.

## 4. Frontend on Vercel

The web client is [bloodbank-web](https://github.com/kiins3/bloodbank-web) (Next.js).

1. Vercel → **Add New → Project** → import `bloodbank-web`. The framework is detected as Next.js.
2. Add the environment variable `NEXT_PUBLIC_API_URL` = `https://<service>.onrender.com` (no trailing slash), then deploy.
3. If the project domain is not `bloodbank-web.vercel.app`, add it to `CORS_ALLOWED_ORIGINS` on Render (comma separated) and redeploy the API.

## 5. Demo data

The seed script needs direct database access for one step, so seed locally and copy the result to Aiven:

```
docker compose down -v && docker compose up -d
python scripts/seed_demo.py
docker compose exec -T mysql sh -c 'mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" --no-tablespaces --set-gtid-purged=OFF BloodBankDB' > demo.sql
docker compose exec -T mysql mysql -h <aiven-host> -P <port> -u avnadmin -p<password> --ssl-mode=REQUIRED defaultdb < demo.sql
```

Import before the API's first start on Render, or restart the API afterwards. `demo.sql` is ignored by git.

## Keeping it awake (optional)

Free Render services sleep when idle. A free monitor such as [UptimeRobot](https://uptimerobot.com) pinging `/actuator/health` every 10 minutes keeps it warm during job-hunting season.
