$ ->
  $("#update_form").on("click", "#activiti_btn",(r) ->
    $btn = $(@)
    if checkReview()
      return unless confirm('确认提交?')
      $form = $("#update_form")
      $form.attr("action", "/procureunits/submitactiviti")
      $form.submit()
  ).on('click', 'a[name=terminateProcess]', (r) ->
    $btn = $(@)
    $form = $('#update_form')
    if checkReview()
      return unless confirm('确定终止流程?')
      window.top.location.href = $btn.data('action') + "?" + $form.serialize()
  )

  # 检查审核意见
  checkReview = ->
    review = $('#opition').val()
    if (review == "" || review == undefined) && !$("#isEnd").val()
      alert "审批意见必填！"
      false
    else
      true