Preciso que gere um microsservico Java 21 com Springboot que receba uma lista de textos de um front end web para tradução, traduza estes textos e então retorne.
Utilize os padrões Pipeline Pattern e Strategy Pattern na solução (se houver algum outro, inclua e justifique).
Assuma que no fluxo eu teria passos como: remover duplicidades, remover dados sensíveis, consultar cache, enviar para tradução, etc.
Assuma também que eu eventualmente posso receber documentos ou imagens para tradução.

Developer: # Objetivo
Desenvolver um microsserviço em Java 21 utilizando Spring Boot, que recebe uma lista de textos, documentos ou imagens enviados por um front-end web para tradução, processa-os conforme um fluxo configurável e retorna os resultados ao front-end.

Begin with a concisa checklist (3-7 bullets) das etapas principais antes de iniciar o desenvolvimento; mantenha os itens conceituais, não no nível de implementação.

# Requisitos Técnicos
- O microsserviço deve utilizar os padrões Pipeline Pattern e Strategy Pattern. Caso avalie a necessidade de outros padrões de projeto, adicione-os e justifique sua escolha.
- O pipeline de processamento deve prever etapas como: remoção de duplicidades, eliminação de dados sensíveis, consulta a cache e envio para serviço de tradução. A solução deve ser extensível para suportar a adição ou remoção dessas etapas de forma flexível.
- O serviço deve suportar processamento de diferentes tipos de itens: textos, documentos e imagens. Pode ocorrer mistura desses tipos em uma única requisição.

Após cada etapa de processamento (como remoção de duplicidades ou tradução), valide o resultado em 1-2 linhas e decida prosseguir ou corrigir, se necessário.

# Formato da Requisição
O endpoint principal deve aceitar requisições HTTP `POST` com payload JSON conforme o modelo:
```json
{
  "items": [
    {
      "type": "text", // "text", "document" ou "image"
      "id": "item1",  // identificador único opcional para rastreamento
      "content": "Texto a ser traduzido" // para textos
      // para documentos: arquivo codificado em Base64
      // para imagens: arquivo codificado em Base64
      // campos extras podem ser adicionados conforme o tipo
    }
    // ... outros itens
  ]
}
```
- Para documentos ou imagens, o campo "content" deve conter o conteúdo em Base64.
- A lista "items" pode incluir diferentes tipos no mesmo envio, indicados pelo campo "type".

# Formato da Resposta
## Sucesso
```json
{
  "results": [
    {
      "id": "item1", // Identificador único do item
      "originalType": "text", // "text", "document" ou "image"
      "translatedContent": "Texto traduzido ou arquivo Base64 com tradução",
      "pipelineSteps": ["deduplication", "sensitiveDataRemoval", "cacheHit", "translation"] // etapas do pipeline aplicadas
    }
    // ... outros itens
  ]
}
```
- A ordem dos itens na resposta deve corresponder à ordem de envio.
- O campo "pipelineSteps" deve listar as etapas efetivamente aplicadas ao processamento de cada item, por exemplo: "deduplication", "sensitiveDataRemoval", "cacheHit", "translation".

## Erro
```json
{
  "errors": [
    {
      "id": "item3", // Identificador do item com erro
      "errorType": "TRANSLATION_FAILED", // ou FILE_UNREADABLE, INVALID_TYPE, etc.
      "message": "Descrição do erro no processamento deste item"
    }
    // ... outros erros
  ],
  "results": [/* resultados válidos, caso existam */]
}
```
- Caso algum item apresente erro no pipeline (tradução falhou, arquivo ilegível etc.), reporte no campo "errors", informando o id do item afetado.
- Os itens processados com sucesso devem ser retornados normalmente no campo "results", mesmo havendo erros em outros.

# Observações
- O pipeline deve ser extensível para inserir passos adicionais conforme necessidades futuras.
- Caso utilize outros padrões de projeto além de Pipeline e Strategy, justifique as escolhas.