$ ->
  $(document).on('click', '#exceldown', (e) ->
    e.preventDefault()
    $btn = $(@)
    $form = $("#search_form")
    $form.attr('action', $btn.data('uri'))
    $form.submit()
  ).on('click', '#searchdata', (e) ->
    e.preventDefault()
    $btn = $(@)
    $form = $("#search_form")
    $form.attr('action', $btn.data('uri'))
    $form.submit()
  ).on('change', '#select_country', (e) ->
    countryCode = $(@).val()
    $.post('/shipmentReports/queryCenterIdByCountryCode?a.countryCode='+countryCode, null, (r) ->
      $("#select_centerId").empty()
      $("#select_centerId").append("<option value=''>centerID</option>")
      _.each(r, (value) ->
        $("#select_centerId").append("<option value='#{value}'>#{value}</option>")
      )
    )
  )

