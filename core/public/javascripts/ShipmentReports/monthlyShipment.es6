$(() => {

  $("#download_excel").click(function (e) {
    e.stopPropagation();
    let $form = $("#search_form");
    window.open($(this).data("url") + "?" + $form.serialize(), "_blank");
  });

});