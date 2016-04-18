$ ->
  $("#unitEditForm").on("click", "#activiti_btn", (r) ->
    $btn = $(@)
    if checkReview()
      return unless confirm('确认提交?')
      $form = $("#unitEditForm")
      $form.attr("action", "/procureunits/submitactiviti")
      $form.submit()
  ).on('click', 'a[name=terminateProcess]', (r) ->
    $btn = $(@)
    $form = $('#unitEditForm')
    if $("#isEnd").val()=="true"
      alert "已审核通过，不可终止流程！"
    else
      if checkReview()
        return unless confirm('确定终止流程?')
        window.top.location.href = $btn.data('action') + "?" + $form.serialize()
  )

  # 检查审核意见
  checkReview = ->
    review = $('#opition').val()
    if !$("#isEnd").val()
      if _.isEmpty(review)
        alert "审批意见必填！"
        false
      else
        true
    else
      true