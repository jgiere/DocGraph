# Ersetzt den Typen des Feldes "attr_*" mit dem neu ersten Fieldtype.
curl -X POST -H 'Content-type:application/json' --data-binary '{
  "replace-dynamic-field":{
     "name":"attr_*",
     "type":"text_de_wm",
     "termVectors": "true",
     "termPositions": "true",
     "termOffsets": "true",
     "stored": "true",
     "indexed": "true" }
}' http://localhost:8983/solr/wm/schema
