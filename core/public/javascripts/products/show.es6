$(() => {

  $("#search_btn").click(function (e) {
    e.preventDefault();

    let search = $("intput[name='p.search']").val();
    $("#data_div").load("", {search: search});
  });

});