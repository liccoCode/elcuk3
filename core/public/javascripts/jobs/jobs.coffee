$ ->
  $('a[rel=tooltip]').tooltip()

  # 设置 params 的 j_close 值
  setJobClosedVal = (params, selector) ->
    params['j.close'] = $(selector).is(':checked')

  # 添加 Job 按钮
  $('#add_job_btn').click ->
    $.varClosure.params = {}
    $('#job_add_form :input').map($.varClosure)
    # 自行修正 Checkbox 的值
    setJobClosedVal($.varClosure.params, '#j_close')
    $.post('/jobs/c', $.varClosure.params,
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
    jid = $(@).attr('jid')
    $.varClosure.params = {}
    $('#job_itm_' + jid + ' :input').map($.varClosure)
    setJobClosedVal($.varClosure.params, "#job_itm_" + jid + " :input[name='j.close']")
    $.post('/jobs/u', $.varClosure.params,
      (r) ->
        if r.flag is false
          alert('更新失败! ' + r.message)
        else
          alert(r['className'] + ' 更新成功') if r['className']
    )
