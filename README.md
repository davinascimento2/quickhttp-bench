# 🚀 QuickHttp Bench

[![Java 21](https://img.shields.io/badge/Java-21%20LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![CI Build](https://img.shields.io/badge/CI-Passing-brightgreen?style=for-the-badge&logo=githubactions&logoColor=white)](.github/workflows/ci.yml)
[![Zero Dependencies](https://img.shields.io/badge/Dependencies-Zero%20External-blue?style=for-the-badge)](#)
[![License](https://img.shields.io/badge/License-MIT-purple?style=for-the-badge)](LICENSE)

> Asynchronous HTTP load testing and latency percentile (p50/p90/p95/p99) benchmark CLI in Java 21.

---

## ⚡ Como Usar

### Compilação Rápida
```bash
# Compilar direto com javac
javac -d target/classes $(find src/main/java -name "*.java")
jar --create --file target/quickhttp-bench.jar --main-class com.allday.quickhttp.QuickHttp -C target/classes .

# Executar
java -jar target/quickhttp-bench.jar
```

---

## 🛠️ Tecnologias
- **Java 21 LTS** puro (Records, Pattern Matching, Streams, Lambdas)
- Zero dependências de terceiros no runtime
- GitHub Actions CI/CD automatizado

---

## 👤 Autor
Desenvolvido por **Davi Nascimento**
- GitHub: [@davinascimento2](https://github.com/davinascimento2)
