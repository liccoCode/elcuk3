$ ->
  $("table").on('click', 'a#replaceUnitFBA', (e) ->
    return if !confirm('确定?')
    LoadMask.mask()
    $btn = $(@)
    $.getJSON('/FBAs/plan', {unitId: $btn.data('unit-id')}, (r) ->
      fba = r.fba
      if _.isEmpty(fba)
        noty({
          text: r.message,
          type: 'error',
          timeout: 3000
        })
      else
        result = confirm("是否采用? FBA[ShipmentId: #{fba.shipmentId}, CenterId: #{fba.centerId}]")
        $.getJSON('/FBAs/confirm', {
          unitId: $btn.data('unit-id'),
          fbaId: fba.id,
          result: result
        }, (r) ->
          if r.flag
            if result
              noty({
                text: '成功确认 FBA! 即将刷新页面...',
                type: 'success',
                timeout: 3000
              })
          else
            noty({
              text: r.message,
              type: 'error',
              timeout: 3000
            })
        )
      LoadMask.unmask()
      setTimeout(->
        location.reload()
      , 2000)
    )
  ).on('click', 'a[name=checkFBALabel]', (e) ->
    $('#fba_ship_to_body').html(new FbaShipToBuilder($(@)).buildBody())
    $('#fba_ship_to_modal').modal('show');
  )
  $('#unit_table').on('click', 'a[name=confirmUnitBtn]', (e) ->
    $btn = $(@)
    $.getJSON($btn.data('href'), (data) ->
      if data.flag
        $("##{$btn.data('tragettr')}").find("td[name=isConfirm]>span>img").attr('src', '/img/green.png')
        noty({
          text: '更新成功',
          type: 'success',
          timeout: 3000
        })
      else
        noty({
          text: data.message,
          type: 'error',
          timeout: 3000
        })
    )
  )

  $('#unit_table').on('click', 'a[name=noPaymentBtn]', (e) ->
    e.preventDefault()
    $btn = $(@)
    $.getJSON($btn.data('href'), (data) ->
      if data.flag
        noty({
          text: '设置成功',
          type: 'success',
          timeout: 3000
        })
        if $btn.text() == '统计'
          $btn.prop("class", "btn btn-xs btn-danger")
          $btn.text('不统计')
        else
          $btn.prop("class", "btn btn-xs btn-success")
          $btn.text('统计')
      else
        noty({
          text: data.message,
          type: 'error',
          timeout: 3000
        })
    )
  )

  class FbaShipToBuilder
    constructor: ($a) ->
      @addressLine1 = $a.data('addressline1')
      @addressLine2 = $a.data('addressline2')
      @city = $a.data('city')
      @name = $a.data('name')
      @countryCode = $a.data('countrycode')
      @stateOrProvinceCode = $a.data('stateorprovincecode')
      @postalCode = $a.data('postalcode')

    countryCodeMap: ->
      return {
        "GB": "United Kingdom",
        "US": "United States",
        "CA": "Canada",
        "CN": "China (Mainland)",
        "DE": "Germany",
        "FR": "France",
        "IT": "Italy",
        "JP": "Japan"
      }

    formatCountryCode: ->
      return @countryCodeMap[@countryCode]

    buildBody: ->
      body = "<b> SHIP TO:</b><br>";
      if !_.isEmpty(@name) then body += "<b>#{@name}</b><br>"
      if !_.isEmpty(@addressLine1) then body += "<b>#{@addressLine1}</b><br>"
      if !_.isEmpty(@addressLine2) then body += "<b>#{@addressLine2}</b><br>"
      if !_.isEmpty(@city) then body += "<b>#{@city},</b>&nbsp;"
      if !_.isEmpty(@stateOrProvinceCode) then body += "<b>#{@stateOrProvinceCode}</b>&nbsp;"
      if !_.isEmpty(@postalCode) then body += "<b>#{@postalCode}</b><br>"
      if !_.isEmpty(@countryCode)
        code = @formatCountryCode()
        if !_.isEmpty(code) then body += "<b>#{code}</b><br>"
      return body;

  $(document).ready ->
    for trigger in $("i[name=showFeedsPage]")
      $trigger = $(trigger)
      if $trigger.data('feeds-page')
        pageKey = $trigger.data('feeds-page')
        $page = $("table[data-feeds-page-key='#{pageKey}']")
        $trigger.popover({
          html: true,
          trigger: "click",
          placement: "right",
          content: $page.html(),
          title: "Feeds"
        }).popover('hide')


