/**
 * Created by licco on 2016/12/2.
 */

$(() => {

  $("#new_inbound input[name$='qty']").change(function() {
    if ($(this).val() == $(this).data('qty')) {
      $(this).parent('td').next().find('select').hide();
    } else {
      $(this).parent('td').next().find('select').show();
    }
  });

});