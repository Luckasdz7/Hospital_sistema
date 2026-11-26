<div align="center">

# 🏥 Sistema de Gestão Hospitalar

**Aplicação Desktop completa para gerenciamento clínico, administrativo e de atendimentos médicos, desenvolvida em Java com arquitetura em camadas.**

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Java Swing](https://img.shields.io/badge/Interface-Java%20Swing-007396?style=for-the-badge)
![Maven](https://img.shields.io/badge/Apache%20Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![NetBeans](https://img.shields.io/badge/IDE-Apache%20NetBeans-1B6AC6?style=for-the-badge&logo=apachenetbeanside&logoColor=white)
![Status](https://img.shields.io/badge/Status-Em%20Desenvolvimento-brightgreen?style=for-the-badge)

</div>

---

## 📑 Índice

- [Sobre o Projeto](#-sobre-o-projeto)
- [Funcionalidades (Requisitos Funcionais)](#-funcionalidades-requisitos-funcionais)
- [Engenharia Interna (Requisitos Não-Funcionais e Regras de Negócio)](#-engenharia-interna-requisitos-não-funcionais-e-regras-de-negócio)
- [Arquitetura e Estrutura de Pastas](#-arquitetura-e-estrutura-de-pastas)
- [Tecnologias Utilizadas](#-tecnologias-utilizadas)
- [Download do Executável (Instalador)](#-download-do-executável-instalador)
- [Como Executar o Código-Fonte](#-como-executar-o-código-fonte)
- [Testes Automatizados](#-testes-automatizados)
- [Autor](#-autor)

---

## 💡 Sobre o Projeto

O **Sistema de Gestão Hospitalar** foi projetado para centralizar e automatizar a rotina de clínicas e hospitais. O sistema separa o fluxo de trabalho por **níveis de permissão**, garantindo que administradores, secretárias e médicos acessem exatamente as ferramentas necessárias para suas funções, mantendo a integridade das consultas, cadastros e prescrições médicas.

---

## 🚀 Funcionalidades (Requisitos Funcionais)

O sistema conta com controle de acesso via autenticação (`Login`) e divide suas operações em três painéis dedicados:

### 🔐 Autenticação e Controle de Acesso (`Login`)
- Validação de credenciais para entrada no sistema.
- Redirecionamento automático para o módulo correspondente ao perfil do usuário autenticado (**Administrador**, **Secretária** ou **Médico**).

### 🛠️ Painel do Administrador (`Form_admin`)
- **Gestão Completa de Usuários e Equipe:** Cadastro, consulta, edição e remoção (CRUD) de **Funcionários** e **Médicos**.
- **Gestão de Pacientes:** Controle total da base de pacientes cadastrados.
- **Visão Geral de Consultas e Receitas:** Supervisão completa dos registros operacionais do hospital.

### 📋 Painel da Secretária / Recepção (`Form_Secretaria`)
- **Cadastro e Atendimento de Pacientes:** Registro e atualização de dados cadastrais de pacientes na recepção.
- **Agendamento de Consultas:** Marcação, remarcação, acompanhamento e gerenciamento de status das consultas médicas.

### 🩺 Painel do Médico (`Form_medico`)
- **Agenda de Atendimentos:** Visualização e gerenciamento das consultas vinculadas ao profissional.
- **Emissão de Receitas:** Criação, vinculação e consulta de prescrições médicas (`receitas`) associadas ao atendimento do paciente.

### 📊 Matriz de Permissões

| Módulo / Ação | Administrador | Secretária | Médico |
| :--- | :---: | :---: | :---: |
| Autenticação no Sistema (`Login`) | ✅ | ✅ | ✅ |
| Gestão de Funcionários | ✅ | ❌ | ❌ |
| Gestão de Médicos | ✅ | ❌ | ❌ |
| Gestão de Pacientes | ✅ | ✅ | 👁️ |
| Agendamento e Controle de Consultas | ✅ | ✅ | ✅ |
| Emissão e Gestão de Receitas | ✅ | ❌ | ✅ |

---

## ⚙️ Engenharia Interna (Requisitos Não-Funcionais e Regras de Negócio)

Além das telas visíveis ao usuário, o projeto implementa boas práticas de engenharia de software "por baixo dos panos":

### 🛡️ Requisitos Não-Funcionais (RNF)
- **Arquitetura em Camadas (Separação de Responsabilidades):** O código isola completamente a interface gráfica (`FORMS`), as regras de negócio (`BO`), a comunicação com o banco de dados (`DAO`) e os modelos de dados (`MODEL`), facilitando a manutenção e futuras expansões.
- **Segurança e Segregação de Perfis:** O acesso às funcionalidades é restrito conforme o nível do usuário autenticado, evitando acessos indevidos a áreas administrativas ou clínicas.
- **Integridade e Padronização de Dados:** Uso de tipos enumerados (`Enum`) para restringir valores críticos no domínio, como `Sexo` e `enum_consultas` (padronizando estados/tipos de consulta e evitando dados inconsistentes no banco).
- **Confiabilidade e Qualidade (Testabilidade):** O fluxo central de consultas é coberto por testes automatizados (`ConsultasFluxoTest`), garantindo que regras críticas continuem funcionando após alterações no código.
- **Conexão Centralizada:** O gerenciamento da conexão com o banco de dados é encapsulado na classe utilitária `UTIL.Conexao`, evitando duplicação de credenciais e facilitando a troca de ambiente.
- **Portabilidade:** Desenvolvido em Java com gerenciamento via Maven, podendo ser compilado e empacotado como aplicativo nativo/executável.

### 📏 Camada de Regras de Negócio (`BO`)
Antes de qualquer dado chegar ao banco de dados, ele passa pela camada de **Business Objects (`BO`)**:
- `BOConsultas`: Validações do ciclo de vida, conflitos e regras de agendamento/alteração de consultas.
- `BOPacientes`, `BOMedicos` e `BOFuncionarios`: Validação de campos obrigatórios, formatação e integridade cadastral.
- `BOReceitas`: Regras de validação e vínculo para emissão de prescrições médicas.

---

## 🏗️ Arquitetura e Estrutura de Pastas

O fluxo de dados do sistema segue o padrão:

```text
[ FORMS (Interface Swing) ] ──▶ [ BO (Regras de Negócio) ] ──▶ [ DAO (Banco de Dados) ] ──▶ [ Banco Relacional ]
                                            │                             │
                                            └───────▶ [ MODEL ] ◀─────────┘
```

### 📂 Organização dos Arquivos

```text
hospital_sistema/
├── pom.xml                                # Configurações e dependências do Maven
├── README.md                              # Documentação do projeto
└── src/
    ├── main/
    │   └── java/
    │       ├── com/mycompany/hospital/
    │       │   └── Hospital.java          # Classe principal (ponto de entrada)
    │       ├── MODEL/                     # Entidades e Enums do domínio
    │       │   ├── Sexo.java
    │       │   ├── enum_consultas.java
    │       │   ├── pacientes.java
    │       │   ├── medicos.java
    │       │   ├── funcionarios.java
    │       │   ├── consultas.java
    │       │   └── receitas.java
    │       ├── DAO/                       # Data Access Objects (Persistência SQL)
    │       │   ├── DAOpacientes.java
    │       │   ├── DAOMedicos.java
    │       │   ├── DAOfuncionarios.java
    │       │   ├── DAOconsultas.java
    │       │   └── DAOreceitas.java
    │       ├── BO/                        # Business Objects (Regras de Negócio)
    │       │   ├── BOPacientes.java
    │       │   ├── BOMedicos.java
    │       │   ├── BOFuncionarios.java
    │       │   ├── BOConsultas.java
    │       │   └── BOReceitas.java
    │       ├── FORMS/                     # Interfaces Gráficas (Java Swing / NetBeans)
    │       │   ├── Login.java (.form)
    │       │   ├── Form_admin.java (.form)
    │       │   ├── Form_Secretaria.java (.form)
    │       │   └── Form_medico.java (.form)
    │       └── UTIL/
    │           └── Conexao.java           # Fábrica de conexão com o banco de dados
    └── test/
        └── java/
            └── ConsultasFluxoTest.java    # Testes automatizados do fluxo de consultas
```

---

## 🛠️ Tecnologias Utilizadas

- **Linguagem:** Java (JDK 14+)
- **Interface Gráfica:** Java Swing (criada com NetBeans GUI Builder)
- **Gerenciador de Dependências e Build:** Apache Maven
- **Persistência:** JDBC + Banco de Dados Relacional *(ex: MySQL / PostgreSQL)*
- **IDE Recomendada:** Apache NetBeans

---

## 📦 Download do Executável (Instalador)

Se você deseja apenas **usar o sistema no Windows** sem precisar abrir o código-fonte:

1. Acesse a aba **[Releases](https://github.com/luckasdz7/hospital_sistema/releases)** deste repositório.
2. Baixe o instalador (`.exe`) ou o pacote portátil (`.zip`) da versão mais recente.
3. Importe o script do banco de dados disponível nos *Assets* do Release (caso esteja usando banco local).
4. Execute o instalador e inicie o **Sistema Hospitalar**.

---

## 💻 Como Executar o Código-Fonte

### Pré-requisitos
- **Java JDK** (versão 14 ou superior) instalado.
- **Apache NetBeans** (ou outra IDE Java com suporte a Maven).
- Servidor de **Banco de Dados** instalado e em execução.

### Passo a passo

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/luckasdz7/hospital_sistema.git
   ```

2. **Configure o Banco de Dados:**
   - Crie o banco de dados no seu SGBD e execute o script de criação das tabelas.
   - Abra o arquivo `src/main/java/UTIL/Conexao.java` e ajuste a `URL`, `usuário` e `senha` conforme o seu ambiente local.

3. **Abra no NetBeans:**
   - Vá em `File` > `Open Project` e selecione a pasta `hospital_sistema`.
   - Clique com o botão direito no projeto e selecione **Clean and Build** para baixar as dependências do Maven.

4. **Execute a aplicação:**
   - Execute o projeto diretamente pelo NetBeans (classe principal `com.mycompany.hospital.Hospital` ou `FORMS.Login`).

---

## 🧪 Testes Automatizados

O projeto possui testes automatizados para validar o fluxo de consultas (`src/test/java/ConsultasFluxoTest.java`). Para executá-los via terminal na raiz do projeto:

```bash
mvn test
```

Ou, no NetBeans, clique com o botão direito sobre o projeto e selecione **Test**.

---

## 👤 Autor

Desenvolvido por **Lucas** ([@luckasdz7](https://github.com/luckasdz7)).

<div align="center">
  <sub>Se este projeto te ajudou ou chamou sua atenção, considere deixar uma ⭐ no repositório!</sub>
</div>
