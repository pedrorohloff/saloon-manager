# Cabeleleila Leila Salão de Beleiza

O aplicativo Saloon-Manager tem com objetivo principal a gestão do salão de beleza _Cabeleleila Leila Salão de Beleiza_, possuindo ferramentas de agendamento de serviços presentes no salão e outras ferramentas organizacionais e gerenciais.

O aplicativo possui as seguintes funções principais: 
- Gerenciamento de Agendamento (Alterar Status e outros) via Painel de Administrador; 
- Acompanhamento do desempenho do salão com renda semanal confirmada e projeção de renda mensal;
- Agendamento de Serviços no Painel de Clientes;
- Sugestão de reagendamento automático para clientes com mais de três agendamentos na mesma semana;
- Histórico de agendamentos realizados por cliente;
- Filtro de Data para o histórico de agendamentos;
- Edição de agendamentos para clientes;
- Visualização de detalhes do agendamento;

## Artefatos (Imagens e Videos)

Todos os artefatos gerados estão presentes na pasta `imagens-e-videos`.<br><br>
**OBS:** Tive um problema na gravação e as cores aparecem com glitch e artefatos, isso não se replica na aplicação, apenas no vídeo de demonstração.

## Tecnologias Utilizadas e Observações

Conforme a restrição de tempo imposta para a entrega do aplicativo, foi decido a utilização da seguinte stack:

- **Java 25**<br>
  Essa tecnologia foi escolhida pois além de ser uma ótima opção para o caso, é a linguagem que possuo maior experiência na construção de aplicativos Web.

- **Spring Boot 4.0.6**<br>
  Essa tecnologia foi escolhida para facilitar o desenvolvimento web, contendo diversas funcionalidades embarcadas em seu kit de desenvolvimento (Spring Web, Spring Security, etc).

- **Vaadin Flow 25.1.5**<br>
  Essa tecnologia foi escolhida por sua facilidade e rapidez no desenvolvimento de uma interface de usuário utilizando diretamente Java, sem precisar realizar a separação entre backend e frontend, agilizando o desenvolvimento.

- **PostgreSQL 16**<br>
  Banco de dados relacional com ótimo desempenho, além disso, o modelo relacional cumpre o seu papel perfeitamente no caso desse aplicativo, realizando o relacionamento entre agendamentos e serviços.
  
- **Maven**<br>
  Ferramenta extremamente comum para o gerenciamento de pacotes e sistema de build (/mvnw). 

- **Docker / Docker Compose**<br>
  Facilita o desenvolvimento e organização do projeto, onde o banco de dados e aplicação podem ser rodadas dentro de containers pré-configurados, pulando qualquer configuração extra de ambiente de desenvolvimento.


## Project Structure

O projeto segue a seguinte arquitetura:

```text
saloon-manager/
├── Dockerfile
├── docker-compose.yaml
├── LICENSE.md
├── pom.xml
├── README.md
├── mvnw
├── mvnw.cmd
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/
    │   │       └── example/
    │   │           ├── Application.java
    │   │           ├── data/
    │   │           │   ├── entity/
    │   │           │   │   ├── Appointment.java
    │   │           │   │   ├── AppointmentStatus.java
    │   │           │   │   ├── RoleType.java
    │   │           │   │   ├── ServiceEntity.java
    │   │           │   │   └── User.java
    │   │           │   └── repository/
    │   │           │       ├── AppointmentRepository.java
    │   │           │       ├── ServiceEntityRepository.java
    │   │           │       └── UserRepository.java
    │   │           ├── infrastructure/
    │   │           │   ├── DatabaseInitializer.java
    │   │           │   └── security/
    │   │           │       └── SecurityConfig.java
    │   │           ├── services/
    │   │           │   ├── AppointmentService.java
    │   │           │   ├── BusinessDashboardStats.java
    │   │           │   ├── GroupingRecommendation.java
    │   │           │   ├── ServiceEntityService.java
    │   │           │   ├── UserDetailsServiceImpl.java
    │   │           │   └── UserService.java
    │   │           └── views/
    │   │               ├── HomeView.java
    │   │               ├── LoginView.java
    │   │               ├── MainLayout.java
    │   │               ├── RegisterView.java
    │   │               ├── admin/
    │   │               │   ├── AdminDashboardPresenter.java
    │   │               │   ├── AdminDashboardView.java
    │   │               │   └── AdminDashboardViewInterface.java
    │   │               └── client/
    │   │                   ├── ClientDashboardPresenter.java
    │   │                   ├── ClientDashboardView.java
    │   │                   └── ClientDashboardViewInterface.java
    │   └── resources/
    │       ├── application.properties
    │       ├── assets/
    │       │   └── saloon-icon.svg
    │       └── META-INF/
    │           └── resources/
    │               ├── styles.css
    │               ├── view-title.css
    │               └── icons/
    │                   └── clipboard-check.svg
    └── test/
        └── java/
            └── com/
                └── example/
```

O ponto de acesso principal da aplicação é `Application.java`. Essa classe contém o método `main()` que inicia o _Spring Boot application_.

O projeto é estruturado nos seguintes pacotes:
* **data**: Contém as entidades JPA (como `Appointment`, `User`, `ServiceEntity`) e repositórios do Spring Data para acesso ao banco de dados.
* **infrastructure**: Contém configurações do sistema, como configuração de segurança e implantação de dados no banco de dados.
* **services**: Contém o estereótipo `@Service` do Spring, classes com regras de negócio, limites de transações, e records (ex: `AppointmentService`).
* **views**: Contém layouts de interface de usuário e views baseadas no Vaadin Flow, usando o padrão arquitetural MVP (Model-View-Presenter).

## MVP (Model-View-Presenter)

Como podemos ver pela organização do projeto, ele segue o padrão MVP (Model-View-Presenter) nas classes que mais se adequa a sua aplicação.
<br><br>Podemos ver o exemplo das classes `HomeView.java`, `LoginView.java`, `RegisterView.java`, que possuem uma lógica muito simples, logo, nesse caso, a adoção do MVP estrito faria com que tivéssemos várias classes contendo basicamente apenas boilerplate.
<br><br>Entretanto, nas classes em que é possível e recomendado a aplicação do MVP, como as presentes dentro de `views/admin/` e `views/client/`, seus princípios foram aplicados, onde realizei a separação do view em três funções: a `ViewInterface`, que possui o contrato a ser seguido na View, 
o `Presenter`, que age como intermediário entre a `View` e os modelos, mandando os dados recebidos na view para serem processados, e depois os retorna para a view poder mostrá-los, 
e por fim, a `View`, que apresenta os itens na tela, como o grid de agendamentos, telas de login, cadastro, informações como faturamento, etc., funcionando essencialmente como o frontend da aplicação.


## Como Rodar o Projeto

Para rodar o aplicativo e o seu banco de dados usando o comando:

```bash
docker compose up -d
```

Isso vai criar um container contendo o banco de dados, chamado _app-postgres-db-1_, e um container para a aplicação, chamado _saloon-app_.
<br>
Após isso, acesse o seu navegador de preferência navegue até:

```bash
http://localhost:8080/login
```
Para realizar login na plataforma, ou até:

```bash
http://localhost:8080/register
```
Para se registrar na plataforma.

## Mapa de Endpoints

| Rota / Path | Classe Java | Permissão de Acesso                                    | Funcionalidade Principal                                                                 |
| :--- | :--- |:-------------------------------------------------------|:-----------------------------------------------------------------------------------------|
| `/` | `HomeView` | Autenticado         | Redirecionamento baseado no papel (Role) do usuário logado.                              |
| `/login` | `LoginView` | Público       | Tela de autenticação de usuários.                                                        |
| `/register` | `RegisterView` | Público | Tela para novos clientes se cadastrarem no sistema.                                      |
| `/agendamento` | `ClientDashboardView` | Cliente e Admin | Painel do cliente para agendar serviços, visualizar histórico e editar agendamentos.     |
| `/admin` | `AdminDashboardView` | Apenas Administradores | Painel administrativo com KPIs de faturamento e gerenciamento de todos os agendamentos.  |