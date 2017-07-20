curl -X POST -H 'Content-type:application/json' --data-binary '{
  "delete-field-type":{ "name":"text_de_wm" }
}' http://localhost:8983/solr/wm/schema
