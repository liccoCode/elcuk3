$(() => {

  $(document).ready(function () {
    let $div = $("#sid");
    $("#postType").val("sid");
    $div.load("/Analyzes/analyzes", $('#click_param').serialize(), function () {
      $div.find('table').dataTable({
        "sDom": "<'row-fluid'<'col-sm-3'l><'col-sm-9'f>r>t<'row-fluid'<'col-sm-6'i><'col-sm-6'p>>",
        "sPaginationType": "full_numbers",
        "iDisplayLength": 50,
        "aaSorting": [[16, "desc"]],
        "scrollX": true,
        "columnDefs": [
          {
            "width": "150px",
            "targets": [0]
          },
          {
            "width": "100px",
            "targets": [1]
          },
          {
            "width": "28px",
            "targets": [2]
          },
          {
            "width": "28px",
            "targets": [3]
          },
          {
            "width": "28px",
            "targets": [4]
          },
          {
            "width": "28px",
            "targets": [5]
          },
          {
            "width": "28px",
            "targets": [6]
          },
          {
            "width": "28px",
            "targets": [7]
          },
          {
            "width": "28px",
            "targets": [8]
          },
          {
            "width": "28px",
            "targets": [9]
          },
          {
            "width": "28px",
            "targets": [10]
          },
          {
            "width": "15px",
            "targets": [11]
          },
          {
            "width": "15px",
            "targets": [12]
          },
          {
            "width": "15px",
            "targets": [13]
          },
          {
            "width": "15px",
            "targets": [14]
          },
          {
            "width": "15px",
            "targets": [15]
          },
          {
            "width": "15px",
            "targets": [16]
          },
          {
            "width": "15px",
            "targets": [17]
          },
          {
            "width": "15px",
            "targets": [18]
          },
          {
            "width": "15px",
            "targets": [19]
          },
          {
            "width": "15px",
            "targets": [20]
          },
          {
            "width": "35px",
            "targets": [21]
          },
          {
            "width": "35px",
            "targets": [22]
          },
          {
            "width": "15px",
            "targets": [23]
          },
          {
            "width": "45px",
            "targets": [24]
          },
          {
            "width": "30px",
            "targets": [25]
          }
        ]
      });
    })
  });

});