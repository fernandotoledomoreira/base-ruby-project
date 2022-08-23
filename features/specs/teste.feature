#language: pt
#utf-8

@make_all
Funcionalidade: Garantir que o projeto de QA foi criado e está executando com sucesso
  Eu como um QA
  Quero garantir que o projeto foi criado com sucesso e está executando local e no Jenkins
  Para poder realizar as automações da squad

  @basic_test
  Cenário: Rodar um teste básico
    Dado que possua a mensagem "Deu tudo certo"
    Quando o valor da variável de mensagem for exibida no terminal
    Então o projeto foi criado com sucesso