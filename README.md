# Compliance API

API desenvolvida em Java para realizar triagem de compliance de empresas a partir de consultas a servicos externos de sancoes e AML (Anti-Money Laundering). Ao cadastrar uma empresa, o sistema consulta os dois servicos, aplica uma politica de risco e salva o resultado da avaliacao.

## Funcionalidades

- Cadastro de empresas por meio da API REST.
- Consulta automatica de sancoes pelo OpenFeign.
- Consulta automatica de perfil AML e pessoas politicamente expostas (PEP).
- Avaliacao de risco com os status:
  - `APPROVED`
  - `MANUAL_REVIEW`
  - `REJECTED`
- Classificacao do nivel de risco em `LOW`, `MEDIUM`, `HIGH` ou `CRITICAL`.
- Persistencia em repositorio Spring Data em memoria.
- Fallback para o servico de sancoes.
- Arquivos Mockoon para simular as APIs externas.

## Tecnologias

- Java 25
- Spring Boot 4.0.5
- Spring Data REST
- Spring Data KeyValue
- Spring Cloud OpenFeign
- Resilience4j Circuit Breaker
- Lombok
- Gradle
- JUnit 5
- Mockoon

## Arquitetura

O projeto esta organizado em camadas inspiradas em Domain-Driven Design:

- `domain`: entidades, IDs, politicas de risco e contratos de repositorio.
- `application`: caso de uso que executa a triagem de compliance.
- `infrastructure/persistence`: entidade de persistencia, repositorio e handler de eventos.
- `infrastructure/rest`: clientes OpenFeign e DTOs das APIs externas.

Quando uma empresa e criada, o `CompanyEventHandler` chama o caso de uso `AnalyzeCompanyRiskUseCase`. O caso de uso consulta os clientes de sancoes e AML, transforma os resultados em objetos de dominio, aplica a `CompliancePolicy` e persiste a avaliacao.

## Regra de risco

A avaliacao considera os resultados das duas fontes externas:

- **REJECTED/CRITICAL**: existe pelo menos uma sancao com confianca maior que `0.8`.
- **MANUAL_REVIEW**: a empresa tem associacao PEP ou o score AML e maior que `70`.
- **APPROVED**: nenhum dos criterios acima foi encontrado.
- O nivel tambem varia pelo score AML:
  - ate `30`: `LOW`
  - de `31` a `70`: `MEDIUM`
  - acima de `70`: `HIGH`
  - avaliacao rejeitada: `CRITICAL`

## Como executar

### Pre-requisitos

- JDK 25
- Gradle Wrapper incluso no projeto
- Mockoon para subir as APIs simuladas

### 1. Iniciar as APIs Mockoon

Importe os arquivos:

- `src/main/resources/mockoon_kyc.json` na porta `3001`
- `src/main/resources/mockoon_aml.json` na porta `3002`

Se o ambiente executar o projeto fora da maquina configurada em `application.properties`, ajuste os enderecos de acordo:

```properties
spring.cloud.openfeign.client.config.sanction-client.url=http://localhost:3001
spring.cloud.openfeign.client.config.aml-client.url=http://localhost:3002
```

### 2. Executar a aplicacao

Windows:

```bat
gradlew.bat bootRun
```

Linux ou macOS:

```bash
./gradlew bootRun
```

A aplicacao sera iniciada na porta `8080`.

## Uso da API

O Spring Data REST expoe o recurso de empresas em:

```text
http://localhost:8080/companies
```

Exemplo de cadastro:

```bash
curl -X POST http://localhost:8080/companies \
  -H "Content-Type: application/json" \
  -d '{"name":"Empresa Exemplo","registrationNumber":"12.345.678/0001-90"}'
```

Consultar empresas:

```bash
curl http://localhost:8080/companies
```

Ao criar uma empresa, a API externa de sancoes consulta:

```text
GET /sanctions/companies/{registrationNumber}
```

E a API externa AML consulta:

```text
GET /aml/v1/screening/{registrationNumber}
```

## Testes

Executar todos os testes:

```bash
./gradlew test
```

No Windows:

```bat
gradlew.bat test
```

## Estrutura do projeto

```text
src/
├── main/
│   ├── java/io/miguelsouza/compliance/
│   │   ├── application/
│   │   ├── domain/
│   │   └── infrastructure/
│   │       ├── persistence/
│   │       └── rest/
│   └── resources/
│       ├── application.properties
│       ├── mockoon_kyc.json
│       └── mockoon_aml.json
└── test/
    └── java/io/miguelsouza/compliance/
```

Este projeto foi desenvolvido durante os estudos do bootcamp DIO Itau Java com IA.
