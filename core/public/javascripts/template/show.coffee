$ ->
  $("#saveDeclareBtn").click((e)->
    e.preventDefault()
    $("#attrForm").attr("action", $(@).data("url"))
    $("#attrForm").submit()
  )