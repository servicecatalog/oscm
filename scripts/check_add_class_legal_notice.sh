#!/usr/bin/env bash

prepend_legal_notice () {
    tmpFile=$(mktemp)
    cat $tmpNotice $CLASS_FILE > $tmpFile
    mv "$tmpFile" $CLASS_FILE
}

set_copyright_dates () {
    YEAR=$(date +'%Y')
    DATE=$(date +'%d-%m-%Y')

    tmpNotice=$(mktemp)
    cat $COPYRIGHT_NOTICE_FILE > $tmpNotice

    sed -i 's/T_YEAR/'$YEAR'/g' $tmpNotice
    sed -i 's/T_DATE/'$DATE'/g' $tmpNotice
}

echo "CHECKING FOR LEGAL NOTICE IN $1..."

CLASS_FILE=$1
COPYRIGHT_NOTICE_FILE=$2
LINE_LIMIT=7

LINES_IN_FILE=$(wc -l $CLASS_FILE | awk '{ print $1 }')

if [ "$LINES_IN_FILE" -lt "$LINE_LIMIT" ] ; then
    linesToRead=$LINES_IN_FILE
else
    linesToRead=$LINE_LIMIT
fi

set_copyright_dates

while IFS='' read -r line || [[ -n "$line" ]] ; do
    linesToRead=$((linesToRead-1))

    if [[ "$(echo "$line" | tr '[:upper:]' '[:lower:]')" == *"copyright fujitsu"* ]] ; then
        echo "LEGAL NOTICE FOUND! :)"
        break
    fi

    if [ "$linesToRead" -eq 0 ] ; then
        echo "LEGAL NOTICE NOT FOUND. PREPENDING..."
        prepend_legal_notice
        break
    fi
done < "$1"

echo "DONE!"