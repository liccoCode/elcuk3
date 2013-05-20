$ ->
  $('#shipConfigForm').on('change', 'select', (e) ->
    form = $('#shipConfigForm')
    market = form.find('[name=market]').val()
    shipType = form.find('[name=shipType]').val()
    dayType = form.find('[name=dayType]').val()

    if market and shipType and dayType
      LoadMask.mask()
      $.get("/elcuk/config/#{market}_#{shipType}_#{dayType}")
        .done((config) ->
          $('#currentVal').val(config.val)
          LoadMask.unmask()
        )
        .fail(-> LoadMask.unmask())
  )
