# Erstellt einen neuen Fieldtype.
curl -X POST -H 'Content-type:application/json' --data-binary '{
  "add-field-type" : {
      "name":"text_de_wm",
      "class":"solr.TextField",
      "positionIncrementGap":"100",
      "analyzer":{
        "tokenizer":
          {
          "class":"solr.StandardTokenizerFactory"
          },
        "filters":[
          { "class": "solr.ApostropheFilterFactory" },
          { "class":"solr.LowerCaseFilterFactory" },
#          { "class":"solr.GermanStemFilterFactory" },
          {
            "class":"solr.StopFilterFactory",
            "format":"snowball",
            "words":"lang/stopwords_de.txt",
            "ignoreCase":"true"
          }
         ]
      }
   }
}'  http://localhost:8983/solr/wm/schema
