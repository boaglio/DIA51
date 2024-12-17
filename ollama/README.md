
# Ollama 

Mudar modelo llama3.2 para prompt maior:

```
$ ollama run llama3.2
>>> /set parameter num_ctx 32768
Set parameter 'num_ctx' to '32768'
>>> /save llama3.2-32k
Created new model 'llama3.2-32k'
>>> /bye
```