**Jiren**
-
Jiren é uma ferramenta de suporte a aplicações que auxilia e provêm segurança as rotinas executadas

---
**Casos de Uso e Funcionalidades**
-

- LiveChat: Auto-atendimento, comunicação dinâmica e centralizada com o usuário através do whatsapp

![img_1.png](src/main/resources/META-INF/resources/screens/img_1.png)

- Editor SQL: Execução de scripts SQL através de templates pré-criados/aprovados com logs

![img.png](src/main/resources/META-INF/resources/screens/img.png)

- Automações SQL: Execução de scripts agendadas com logs

![img_2.png](src/main/resources/META-INF/resources/screens/img_2.png)

- Monitoramentos: Monitoramento dos processos e serviços centralizados (HTTP, SQL, NOSQL, SQS, CLOUDWATCH e SAT)

![img_3.png](src/main/resources/META-INF/resources/screens/img_3.png)

---
**Frameworks & Tecnologias**
-
**Frameworks:** 
- Springboot
- Vaadin

**Sistema de Build:** Maven

**Linguagem:** Kotlin (Java 17)

**Banco de Dados:** MySQL

**Autenticação:** AWS Cognito

**Credenciais:** AWS Secrets Manager

---
**Config**
-
**Ambiente Produção**
- Compile: <*mvn clean package -P prod*>
- Start: java -jar jiren.jar

A gestão das credenciais utilizadas pela aplicação é realizada através do AWS Secrets Manager, são necessárias as seguintes credenciais:

Os itens marcados como obrigatórios são essenciais para iniciar o app em produção, os demais são referentes a funcionalidades do app

- database-host: App DB Host (Obrigatório)
- database-schema: App DB Schema (Obrigatório)
- database-user: App DB User (Obrigatório)
- database-password: App DB Password (Obrigatório)
- mailer-user: GMail User (Opcional)
- mailer-password: GMail AppPassword (Opcional)
- jira-user: Jira API User (Opcional)
- jira-password: Jira API Token (Opcional)
- databases-credentials: JSON com os passwords dos BDs utilizados na aplicação (Opcional)
- cognito-clientId: Cognito Client ID (Obrigatório)
- cognito-clientSecret: Cognito Client Secret (Obrigatório)
- twilio-clientId: Twilio Client ID (Opcional)
- twilio-clientSecret: Twilio Client Secret (Opcional)

É necessário informar a configuração do certificado ssl que se encontra na maquina de produção no arquivo *src/main/resources/application-prod.properties*

O certificado deve estar no formato .P12

Para converter certificados .PEM para .P12 utilize o comando abaixo, a senha definida na conversão devera ser informada no arquivo *src/main/resources/application-prod.properties*

<*openssl pkcs12 -export -out Cert.p12 -in cert.pem -inkey key.pem*>

**Ambiente Local**
- Para executar o app localmente utilize o profile "dev" na compilação  <*mvn clean compile -P dev*>
- Utilize o comando <*mvn spring-boot:run*> para executar
- Edite o arquivo *src/main/resources/application-dev.properties* e defina as configurações de banco de dados
