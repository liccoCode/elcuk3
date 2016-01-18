$ ->

  $(document).ready ->
    oTable = $("#profit").dataTable(
      sDom: "<'row-fluid'<'span9'l><'span3'f>r>t<'row-fluid'<'span6'i><'span6'p>>"
      bPaginate: false
      aaSorting: [
        [0, "desc"]
      ]
    )


  $("#category").multiselect({
    buttonWidth: '120px'
    nonSelectedText: 'Category'
    maxHeight: 200
    includeSelectAllOption: true
    onChange: ->
      categroys = []
      this.getSelected().each ->
        categroys.push($(@).val())
      $("#category_hidden").val(categroys)
    onSelectAll: ->
      if $("#category").next().find("button").attr("title") != 'Category'
        $("#category_hidden").val($("#category").next().find("button").attr("title"))($("#category").next().find("button").attr("title"))
      else
        $("#category_hidden").val("")
  });

  $("#exceldown").click (e) ->
    e.preventDefault()
    $form = $('#profits_form')
    window.open('/Excels/skuprofit?' + $form.serialize(), "_blank")


