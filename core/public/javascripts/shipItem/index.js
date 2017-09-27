/**
 * Created by licco on 2016/12/6.
 */
$(() => {

  $('#today').click(function (e) {
    $("input[name='p.from']").datepicker("setDate", new Date());
    $("input[name='p.to']").datepicker("setDate", new Date());
  });
});