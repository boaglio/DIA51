 #!/bin/bash

# Diretório onde estão os arquivos .zip (primeiro parâmetro ou diretório corrente)
ZIP_DIR="${1:-.}"

# Diretório temporário para extração
TEMP_DIR="./temp"

# Diretório de saída para arquivos texto (sempre o diretório corrente)
OUTPUT_DIR="."

# Criar diretório temporário se não existir
mkdir -p "$TEMP_DIR"

# Verificar se existem arquivos .zip no diretório especificado
if ! ls "$ZIP_DIR"/*.zip >/dev/null 2>&1; then
    echo "Nenhum arquivo .zip encontrado em $ZIP_DIR"
    exit 1
fi

# Iterar sobre cada arquivo .zip no diretório
for zip_file in "$ZIP_DIR"/*.zip; do
    echo "Processando $zip_file..."

    # Extrair o conteúdo do zip para o diretório temporário
    unzip -q "$zip_file" -d "$TEMP_DIR"

    # Converter cada arquivo HTML extraído para texto puro
    find "$TEMP_DIR" -type f -name "*.html" | while read -r html_file; do
        # Nome do arquivo sem extensão
        base_name=$(basename "$html_file" .html)
        base_name_zip=$(basename "$zip_file" .zip)
        
        # Arquivo de saída em texto (sempre no diretório corrente)
        text_file="$OUTPUT_DIR/$base_name_zip-$base_name.txt"
                
        # Remover tags HTML e salvar como texto
        sed -e 's/<[^>]*>//g' \
            -e '/^Index:$/d' \
            -e '/^\[thread\]$/d' \
            -e '/^\[date\]$/d' \
            -e '/^\[subject\]$/d' \
            -e '/^\[author\]$/d' \
            -e 's/&lt;//g' \
            -e 's/&gt;//g' \
            "$html_file" > "$text_file"

        # sed 's/<[^>]*>//g' "$html_file" > "$text_file"
        echo "Arquivo convertido: $text_file"
    done

    # Limpar o diretório temporário
    rm -rf "$TEMP_DIR"/*

    # Remover o arquivo zip
    rm -f "$zip_file"
    rm -f *-author.txt
    rm -f *-date.txt
    rm -f *-subject.txt
    rm -f *-thread.txt

    echo "Arquivo zip removido: $zip_file"
done

echo "Processamento concluído!"
