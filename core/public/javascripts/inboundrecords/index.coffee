$ ->
  $('checkbox.inboundrecord_check_all').change(e) ->
    o = $(@)
    region = o.attr('id').split('_')[0].trim()
    $("input:checkbox.#{region}").prop("checked", o.prop("checked"))