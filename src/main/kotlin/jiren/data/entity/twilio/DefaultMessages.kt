package jiren.data.entity.twilio

data class DefaultMessages(
    val WELCOME_MESSAGE: String = "Seja bem vindo, como podemos ajudar?\n" +
            "1- Abrir um chamado\n" +
            "2- Ver meus chamados abertos\n" +
            "3- Detalhes do chamado\n" +
            "4- Responder chamado\n" +
            "5- Falar com analista",
    val NEW_TICKET: String = "1",
    val MY_TICKETS: String = "2",
    val TICKET_DETAILS: String = "3",
    val ANSWER_TICKET: String = "4",
    val LIVE_CHAT: String = "5",
    val ASK_TICKET_TITLE: String = "Qual é o assunto ?",
    val ASK_TICKET_DESCRIPTION: String = "Por favor descreva todos os detalhes",
    val ASK_FOR_TICKET_ATTACHMENTS: String = "Deseja enviar algum anexo ? (Sim ou Nao)",
    val ASK_FOR_TICKET_CONFIRMATION: String = "Confirma a abertura do chamado? (Sim ou Nao)",
    val ASK_FOR_TICKET_CONFIRMATION_WITH_ATTACHMENT: String = "Envie os anexos e após terminar, responda SIM para confirmar a abertura do chamado",
    val ASK_FOR_TICKET_OWNER: String = "Informe o seu e-mail?",
    val ASK_FOR_TICKET_OWNER_TO_CONSULT: String = "Informe o seu e-mail?",
    val ASK_FOR_TICKET_NUMBER: String = "Informe o número do chamado? Ex: ST-12345",
    val ASK_FOR_TICKET_NUMBER_TO_ANSWER: String = "Informe o número do chamado? Ex: ST-12345",
    val ASK_FOR_TICKET_COMMENT: String = "Digite sua resposta?",
    val TICKET_LIST_MESSAGE: String = "Estes sao seus chamados em aberto",
    val ASK_TO_WAIT_FOR_CONTACT: String = "Por favor aguarde, em breve um analista ira entrar em contato",
    val NO_AVAILABLE_OPERATOR: String = "No momento não temos nenhum plantonista, se for urgente por favor procure uma liderença",
    //MESSAGE CODES
    val CODE_WELCOME_MESSAGE: String = "WELCOME_MESSAGE",
    val CODE_NEW_TICKET: String = "NEW_TICKET",
    val CODE_MY_TICKETS: String = "MY_TICKETS",
    val CODE_TICKET_DETAILS: String = "TICKET_DETAILS",
    val CODE_ANSWER_TICKET: String = "ANSWER_TICKET",
    val CODE_LIVE_CHAT: String = "LIVE_CHAT",
    val CODE_ASK_TICKET_TITLE: String = "ASK_TICKET_TITLE",
    val CODE_ASK_TICKET_DESCRIPTION: String = "ASK_TICKET_DESCRIPTION",
    val CODE_ASK_FOR_TICKET_ATTACHMENTS: String = "ASK_FOR_ATTACHMENTS",
    val CODE_ASK_FOR_TICKET_CONFIRMATION: String = "ASK_FOR_TICKET_CONFIRMATION",
    val CODE_ASK_FOR_TICKET_CONFIRMATION_WITH_ATTACHMENT: String = "ASK_FOR_TICKET_CONFIRMATION_WITH_ATTACHMENT",
    val CODE_ASK_FOR_TICKET_OWNER: String = "ASK_FOR_TICKET_OWNER",
    val CODE_ASK_FOR_TICKET_OWNER_TO_CONSULT: String = "ASK_FOR_TICKET_OWNER_TO_CONSULT",
    val CODE_ASK_FOR_TICKET_NUMBER: String = "ASK_FOR_TICKET_NUMBER",
    val CODE_ASK_FOR_TICKET_NUMBER_TO_ANSWER: String = "ASK_FOR_TICKET_NUMBER_TO_ANSWER",
    val CODE_ASK_FOR_TICKET_COMMENT: String = "ASK_FOR_TICKET_COMMENT",
    val CODE_TICKET_LIST_MESSAGE: String = "TICKET_LIST_MESSAGE",
    val CODE_CONVERSATION_START: String = "CONVERSATION_START",
    val CODE_ASK_TO_WAIT_FOR_CONTACT: String = "ASK_TO_WAIT_FOR_CONTACT",
    val CODE_NO_AVAILABLE_OPERATOR: String = "NO_AVAILABLE_OPERATOR"
    )