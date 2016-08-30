$ ->
  $("table").on('click', 'a#replaceUnitFBA', (e) ->
    $("#fba_carton_contents_modal").data('unit-source', $(@).data('unit-id'))
  ).on('click', '#sumbitDeployFBAs', (e) ->
    $modal = $("#fba_carton_contents_modal")
    window.location.href("/FBAs/changeFBA?procureUnitId=#{$modal.data('unit-source')}&#{$modal.find(":input").serialize()}")
  )