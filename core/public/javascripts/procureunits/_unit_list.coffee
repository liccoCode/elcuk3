$ ->
  $("table").on('click', 'a#replaceUnitFBA', (e) ->
    $("#fba_carton_contents_modal").data('unit-source', $(@).data('unit-id')).modal('show')
    $("#sumbitDeployFBAs").data('url', $(@).data("url"))
    unitIds = [$(@).data('unit-id')]
    $("#refresh_div").load("/ProcureUnits/fbaCartonContents",
      unitIds: unitIds, ->
      $("input[name='chooseType']").change(->
        radio = $("input[name='chooseType']:checked")
        id = radio.val()
        $("#tr_" + id + " input[name$='boxNum']").val(radio.attr("boxNum"))
        $("#tr_" + id + " input[name$='num']").val(radio.attr("boxSize"))
        $("#tr_" + id + " input[name$='boxSize']").val(radio.attr("boxSize"))
        $("#tr_" + id + " input[name$='lastCartonNum']").val(radio.attr("lastCartonNum"))
        $("#tr_" + id + " input[name$='singleBoxWeight']").val(radio.attr("singleBoxWeight"))
        $("#tr_" + id + " input[name$='length']").val(radio.attr("boxLength"))
        $("#tr_" + id + " input[name$='width']").val(radio.attr("boxWidth"))
        $("#tr_" + id + " input[name$='height']").val(radio.attr("boxHeight"))
      )
    )
  ).on("click", "i[name=showFeedsPage]", (e) ->
  ).on('click', 'a[name=checkFBALabel]', (e) ->
    $('#fba_ship_to_body').html(new FbaShipToBuilder($(@)).buildBody())
    $('#fba_ship_to_modal').modal('show');
  )
  $('#unit_list').on('click', 'a[name=confirmUnitBtn]', (e) ->
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

  $("#fba_carton_contents_modal").on('click', '#sumbitDeployFBAs', (e) ->
    $modal = $("#fba_carton_contents_modal")
    if $modal.data('unit-source')
      window.location.replace("/FBAs/changeFBA?procureUnitId=#{$modal.data('unit-source')}&#{$modal.find(":input").serialize()}")
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


