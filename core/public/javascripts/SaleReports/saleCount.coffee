$ ->
  $(".search_form").on("click", "#search_btn, #export_btn", (r) ->
    $btn = $(@)
    $form = $(".search_form")
    $form.attr("action", $btn.data("url"))
    $form.submit()
  )
