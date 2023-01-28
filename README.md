**Jiren**
-
Uma aplicação de gerenciamento e monitoramento de serviços que permite aos usuários acompanhar o status de vários serviços, realizar atualizações controladas em bancos de dados, agendar tarefas SQL e obter suporte online via WhatsApp.

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
- **Springboot**

Spring Boot é um framework de desenvolvimento Java criado pela Spring Source (agora parte da Pivotal) que facilita a criação de aplicações autônomas, de alto desempenho e prontas para produção. Ele utiliza a popular estrutura Spring Framework e fornece uma série de recursos e ferramentas para ajudar os desenvolvedores a construir aplicações Java de forma rápida e fácil, sem a necessidade de configuração excessiva.

Alguns dos recursos do Spring Boot incluem:

Configuração automática: Spring Boot usa a convenção sobre configuração, o que significa que ele configura automaticamente as coisas para você, sem necessidade de criar arquivos de configuração complexos.

Execução independente: As aplicações Spring Boot podem ser executadas como aplicativos Java regulares, sem a necessidade de um container de aplicativo ou outro software de gerenciamento de configuração.

Ferramentas de desenvolvimento: Spring Boot fornece uma série de ferramentas de desenvolvimento, como o Spring Initializer, que ajuda a criar projetos Spring Boot rapidamente, e o Spring CLI, que permite aos desenvolvedores executar comandos Spring Boot diretamente do terminal.

Suporte para bancos de dados: Spring Boot oferece suporte nativo para vários bancos de dados, incluindo MySQL, PostgreSQL, Oracle e MongoDB, entre outros.

Suporte para segurança: Spring Boot fornece suporte para segurança de aplicativos web, incluindo autenticação e autorização de usuários.

Suporte para testes: Spring Boot fornece suporte para testes unitários e integração, incluindo a biblioteca de testes Spring Test.

- **Vaadin**

Vaadin é uma biblioteca de componentes web open-source para desenvolvimento de aplicativos Java. Ele permite aos desenvolvedores construir aplicativos web baseados em Java de forma rápida e fácil, sem precisar se preocupar com a camada de visualização. Em vez disso, os desenvolvedores podem se concentrar na lógica de negócios do aplicativo.

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

A melhor alternativa para executar um webapp escrito em Java com Spring Boot na AWS é usar o Elastic Beanstalk. Ele é uma plataforma de serviços gerenciados que facilita a implantação, a escalabilidade e o monitoramento de aplicativos web. Ele também integra-se com outros serviços da AWS, como o RDS para banco de dados e o S3 para armazenamento de arquivos. Ele é projetado especificamente para aplicativos Java e suporta o Spring Boot out of the box

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
