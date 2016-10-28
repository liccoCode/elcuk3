$ ->
  $("table").on('click', 'a#replaceUnitFBA', (e) ->
    $("#fba_carton_contents_modal").data('unit-source', $(@).data('unit-id')).modal('show')
  ).on("click", "i[name=showFeedsPage]", (e) ->
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


