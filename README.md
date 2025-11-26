# ☁️ Cloud Compare App

**Open source cloud cost calculator** - Compare AWS, Azure, and GCP pricing instantly.

🌐 **Live Site:** [cloudcompareapp.com](https://cloudcompareapp.com) *(coming soon)*

---

## 🎯 Features

- 🔍 **Real-time pricing comparison** across AWS, Azure, and GCP
- 📊 **Architecture templates** - Web App, Data Pipeline, Serverless, ML workloads
- 💰 **Detailed cost breakdown** by service with interactive charts
- 📈 **Historical pricing trends** and insights
- 🎨 **Modern UI** with dark mode and responsive design
- 🔓 **100% Free** - No authentication, no limits, completely open source

---

## 🏗️ Architecture

**Backend:**
- Java 17 + Spring Boot 3.x
- PostgreSQL (AWS Aurora Serverless v2)
- AWS SDK for pricing data from all cloud providers
- Spring Cache with Caffeine for performance

**Frontend:**
- Next.js 14 + TypeScript
- Tailwind CSS + shadcn/ui components
- Recharts for beautiful data visualization

**Infrastructure:**
- AWS ECS Fargate (serverless containers)
- Application Load Balancer
- CloudFront + S3 for global CDN
- AWS CDK (Infrastructure as Code in Java)

**CI/CD:**
- GitHub Actions for automated deployments
- Docker containerization

---

## 📁 Project Structure
```
cloud-compare-app/
├── backend/          # Spring Boot REST API
├── frontend/         # Next.js React application  
├── infrastructure/   # AWS CDK infrastructure code
├── .github/          # CI/CD workflows
└── docker-compose.yml # Local development environment
```

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Node.js 20+
- Docker Desktop
- AWS CLI configured
- Maven 3.9+

### Local Development
```bash
# Clone repository
git clone https://github.com/Notover1008/cloud-compare-app.git
cd cloud-compare-app

# Start local PostgreSQL
docker-compose up -d

# Run backend (Terminal 1)
cd backend
mvn spring-boot:run

# Run frontend (Terminal 2)
cd frontend
npm install
npm run dev
```

Access the app at: http://localhost:3000

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👤 Author

**Karthik**

- 🌐 Website: [bythesprint.com](https://bythesprint.com)
- 📺 YouTube: [Coming soon]
- 💼 LinkedIn: [Your LinkedIn]

---

## 🙏 Acknowledgments

- Pricing data sourced from official AWS, Azure, and GCP APIs
- Built with ❤️ for the developer community

---

**⭐ Star this repo if you find it useful!**