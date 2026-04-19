# **Project Description **

This project is a **serverless Java integration platform** that synchronises booking data from **Hostaway** with financial records in **QuickBooks Online**. It uses a **hybrid sync model**: real‑time webhooks for instant updates, with automatic failover to **scheduled polling** when webhooks fail, ensuring no reservation or payout is ever missed.

The system runs entirely on **AWS Lambda**, with **API Gateway**, **EventBridge**, **DynamoDB**, and **Secrets Manager** providing a reliable, low‑maintenance backbone. All infrastructure is provisioned using **Terraform**, and the integration logic is written in **Java 17**, including Hostaway ingestion, business rule processing, QuickBooks API calls, OAuth2 token refresh, and idempotent state tracking.

The result is a fully automated, resilient pipeline that keeps operational booking data and accounting records perfectly aligned.


