$ ->
  $(document).on("change", "select[name^=check.result]", (r) ->
    $select = $(@)
    $ship = $("select[name='check.isship']")
    if($select.val() == 'AGREE')
      $ship.val("SHIP").attr("disabled", 'true')
    else if ($select.val() == 'NOTAGREE')
      $ship.val("NOTSHIP").attr("disabled", 'true')
    else
      $ship.removeAttr("disabled")
  )

  $("#update_form").on("click", "#update_btn, #fullupdate_btn, #endactiviti_btn, #submitactiviti_btn, #updateactiviti_btn", (r) ->
    $btn = $(@)
    $ship = $("select[name='check.isship']")
    if($btn.attr("id") == "endactiviti_btn")
      return unless confirm('还原后采购计划将更新为发货，并结束不发货流程.确认提交?')
    else if($btn.attr("id") == "update_btn")
      return unless confirm('确认保存?')
    else if($btn.attr("id") == "updateactiviti_btn")
      return unless confirm('确认保存?')
    else
      return unless confirm('确认提交?')


    $form = $("#update_form")
    $ship = $("select[name='check.isship']")
    $form.attr("action", "/checktasks/#{$btn.attr("id").split("_")[0]}")
    # 提交表单前将下拉项的disabled属性取消掉
    $ship.removeAttr("disabled")
    $form.submit()
    $ship.val("NOTSHIP").attr("disabled", 'true')
  )


  $("#update_form").on("click", "#submitqc_btn", (r) ->
    $btn = $(@)
    $ship = $("select[name='check.isship']")
    if ($ship.val() == 'NOTSHIP')
      return unless confirm('不发货则表示流程流转到采购,确认提交?')
    else
      return unless confirm('确认提交?')

    $form = $("#update_form")
    $form.attr("action", "/checktasks/submitactiviti")
    # 提交表单前将下拉项的disabled属性取消掉
    $ship.removeAttr("disabled")
    $form.submit()
    $ship.val("NOTSHIP").attr("disabled", 'true')
  )


  $("#update_form").on("click", "#submitqcroll_btn", (r) ->
    $btn = $(@)
    return unless confirm('取消费用将流转到采购,确认提交?')

    $form = $("#update_form")
    $form.attr("action", "/checktasks/rollactiviti")
    # 提交表单前将下拉项的disabled属性取消掉
    $form.submit()
  )


  $("#update_form").on("click", "#planactiviti_btn", (r) ->
    $btn = $(@)
    $dealway = $("select[name='check.dealway']")
    if ($dealway.val() == '')
      alert('请填写处理方式!')
      return
    return unless confirm('确认提交?')


    $form = $("#update_form")
    $ship = $("select[name='check.isship']")
    $form.attr("action", "/checktasks/submitactiviti")
    # 提交表单前将下拉项的disabled属性取消掉
    $ship.removeAttr("disabled")
    $form.submit()
    $ship.val("NOTSHIP").attr("disabled", 'true')
  )


  $("#update_form").on("click", "#operateactiviti_btn", (r) ->
    return unless confirm('确认后则表示已确认该采购计划的预计时间,确认提交?')
    $btn = $(@)
    $form = $("#update_form")
    $ship = $("select[name='check.isship']")
    $form.attr("action", "/checktasks/operateupdateprocess")
    $form.submit()
  )

  $("#update_form").on("click", "#updateprocess_btn", (r) ->
    $btn = $(@)
    $form = $("#update_form")
    $form.attr("action", "/procureunits/updateprocess")
    $form.submit()
  )


  $(".form_datetime").datetimepicker({
  format: 'yyyy-mm-dd hh:ii',
  startDate: new Date().getFullYear() - 4 + "-01-01"
  })

  fidCallBack = () ->
    {fid: $("input[name='attid']").val(), p: 'CHECKTASK'}
  dropbox = $('#dropbox')
  window.dropUpload.loadImages(fidCallBack()['fid'], dropbox, fidCallBack()['p'], 'span1')
  window.dropUpload.iniDropbox(fidCallBack, dropbox)


