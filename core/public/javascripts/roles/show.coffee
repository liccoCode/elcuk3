$ ->
  $('.privilege_form').ajaxForm({
  dataType: 'json',
  success: (r) ->
    alert(r.message)
  })

  $(':checkbox').change (e) ->
    $o = $(@)

    $o.parents('div').find(":checkbox[value='" + $o.attr('pid') + "']").prop("checked", $o.prop('checked'))
    pid = $o.parents('div').find(":checkbox[value='" + $o.attr('pid') + "']").attr('pid')
    $o.parents('div').find(":checkbox[value='" + pid + "']").prop("checked", $o.prop('checked'))

    $o.parents('div').find(":checkbox[class='menu" + $o.attr('value') + "']").prop("checked", $o.prop('checked'))
    $o.parents('table').find(":checkbox[class='menu" + $o.attr('value') + "']").trigger('change')
