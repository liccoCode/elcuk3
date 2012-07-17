$ ->
  $('a[rel=tooltip]').tooltip()

  $.checkBox()

  # 添加 Job 按钮
  $('#add_job_btn').click ->
    $.post('/jobs/c', $('#job_add_form :input').fieldSerialize(false),
      (r) ->
        if r.flag is false
          alert(r.message)
        else
          alert('Job [' + r['className'] + '] 添加成功.')
    )
    false

  # 运行一次的按钮
  $('.runOnce').click ->
    $.post('/jobs/now', id: $(@).attr('jid'),
      (r) ->
        if r.flag is true
          alert('执行成功.')
        else
          alert(r.message)
    )

  # 更新 job
  $('.j_update').click ->
    $.post('/jobs/u', $("#job_itm_#{@getAttribute('jid')} :input").fieldSerialize(false),
      (r) ->
        if r.flag is false
          alert('更新失败! ' + r.message)
        else
          alert(r['className'] + ' 更新成功') if r['className']
    )
