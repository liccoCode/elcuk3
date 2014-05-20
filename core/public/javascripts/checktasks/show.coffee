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

  $("#update_form").on("click", "#update_btn, #fullupdate_btn", (r) ->
    $btn = $(@)
    $form = $("#update_form")
    $ship = $("select[name='check.isship']")
    $form.attr("action", "/checktasks/#{$btn.attr("id").split("_")[0]}")
    console.log($("input[name='check.startTime']").val() + "-----------------")
    #$("input[name='startTime']").val($("input[name='check.startTime']").val() + ":00")
    #$("input[name='endTime']").val($("input[name='check.endTime']").val() + ":00")
    # 提交表单前将下拉项的disabled属性取消掉
    $ship.removeAttr("disabled")
    $form.submit()
    $ship.val("NOTSHIP").attr("disabled", 'true')
  )
  fidCallBack = () ->
    {fid: $("input[name='check.id']").text(), p: 'CHECKTASK'}

  dropbox = $('#dropbox')
  window.dropUpload.loadImages(fidCallBack()['fid'], dropbox, fidCallBack()['p'], 'span1')
  window.dropUpload.iniDropbox(fidCallBack, dropbox)


