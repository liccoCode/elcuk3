$ ->
  $("table").on('click', 'a#replaceUnitFBA', (e) ->
    $("#fba_carton_contents_modal").data('unit-source', $(@).data('unit-id')).modal('show')
  ).on("click", "i[name=showFeedsPage]", (e) ->
  )
  $("#fba_carton_contents_modal").on('click', '#sumbitDeployFBAs', (e) ->
    $modal = $("#fba_carton_contents_modal")
    if $modal.data('unit-source')
      window.location.replace("/FBAs/changeFBA?procureUnitId=#{$modal.data('unit-source')}&#{$modal.find(":input").serialize()}")
  )

  $(document).ready ->
    $trigger = $("i[name=showFeedsPage]")
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


