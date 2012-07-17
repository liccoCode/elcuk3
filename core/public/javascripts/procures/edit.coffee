$ ->
# dkdfj
  abc = ""
  $('input[type=date]').dateinput(format: 'yyyy-mm-dd')

  # 更新按钮
  $('#update_procureUnit').click (e) ->
    form = $('#post_form')
    form.mask('更新中...')
    $.post('/procures/update', form.formSerialize(),
      (r) ->
        alertEl = $('#alert')
        if r.flag is false
          alertEl.removeClass('invisible').addClass('alert-error').find('span').html(r.message)
        else
          alertEl.removeClass('invisible').addClass('alert-success').find('span').html('更新成功: ' + JSON.stringify(r))
        form.unmask()
    )
    e.preventDefault()
