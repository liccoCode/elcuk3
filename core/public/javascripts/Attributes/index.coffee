$ ->
  $(document).on("click", "[name^='att_update_btn'],[name^='att_delete_btn']", (e) ->
    $btn = $(@)
    $form = $("##{$btn.data("form")}")
    $form.attr("action", $btn.data("action"))
    $form.submit()
  )

