**SSIJ B2B Backend (Spring Boot + Docker)**
===========================================

Backend API for **Sree South India Jewellers (SSIJ)** B2B Wholesale Jewellery Platform.

The backend provides:

-   Authentication (OTP via Firebase, JWT for admin)

-   Retailer management

-   Stock catalog management

-   Order management

-   Presigned URL generation for Cloudflare R2

-   Worker job dispatch

-   PDF invoice generation

-   Integration with Supabase PostgreSQL

* * * * *

**🚀 Tech Stack**
-----------------

-   **Java 21 (Spring Boot)**

-   **Supabase PostgreSQL**

-   **Cloudflare R2 (S3-Compatible Storage)**

-   **Firebase Authentication & FCM**

-   **Docker & Docker Compose**

-   **Flyway Migrations**

* * * * *

**📁 Folder Structure**
-----------------------

`src/
  main/
    java/
    resources/
      application.yaml
      db/migration/
Dockerfile
pom.xml
README.md`

* * * * *

**🔧 Setup --- Local Development**
--------------------------------

### **1\. Clone the repo**

`git clone https://github.com/akeshya/ssij-backend.git
cd ssij-backend`

### **2\. Create local environment file**

Create `application-local.yaml` or use env variables:

`DATABASE_URL=jdbc:postgresql://localhost:5433/ssij_local
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=password

S3_ENDPOINT=http://localhost:9000
S3_BUCKET=local-bucket
S3_ACCESS_KEY_ID=local
S3_SECRET_ACCESS_KEY=local`

### **3\. Run locally**

`mvn spring-boot:run`

* * * * *

**🐳 Run Using Docker**
-----------------------

`docker build -t ssij-api .
docker run -p 8080:8080 --env-file ./env/api.env ssij-api`

* * * * *

**📦 API Endpoints**
--------------------

-   `/api/v1/auth/**`

-   `/api/v1/users/**`

-   `/api/v1/stock/**`

-   `/api/v1/orders/**`

-   `/api/v1/storage/presign`

* * * * *

**🛡️ Branching & PR Rules**
----------------------------

Follow the global development guide:

-   No direct commits to `main`

-   Use feature branches:\
    `feature/<dev>/<task>`

-   PR required

-   CI must pass

-   Squash & merge only
