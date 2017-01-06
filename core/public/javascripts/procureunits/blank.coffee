$ ->



  $(document).ready ->
    $shipType = $("[name='unit.shipType']")
    $shipType.trigger('change') if $shipType.val() != undefined && $shipType.val() != 'EXPRESS'





