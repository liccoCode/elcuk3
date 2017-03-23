$(() => {

  $.getJSON("/deliveryments/indexPer", function (r) {
    $("#a_units").highcharts('StockChart', {
      credits: {
        text: 'EasyAcc',
        href: ''
      },
      title: {
        text: '每日下单量, 2012-2017'
      },
      subtitle: {
        text: 'Source: easya.cc'
      },
      legend: {
        layout: 'vertical',
        align: 'right',
        verticalAlign: 'middle'
      },
      scrollbar: {
        barBackgroundColor: 'gray',
        enabled: true
      },
      rangeSelector: {
        enabled: true,
        selected: 2
      },
      xAxis: {
        title: {
          text: 'daily num'
        },
        type: 'datetime'
      },
      yAxis: {
        title: {
          text: 'Number of Employees'
        },
        min: 0
      },
      plotOptions: {
        series: {
          pointStart: 2000
        }
      },
      tooltip: {
        Shared: false,
        shadow: true
      },
      series: r['series']
    });
  });

  $.getJSON("/deliveryments/perCreateTotalNum", function (r) {
    $("#a_turn").highcharts({
      credits: {
        text: 'EasyAcc',
        href: ''
      },
      title: {
        text: 'Per Total ProcureUnit Num. 2012-2017'
      },

      series: r['series']
    });
  });

  $.getJSON("/payments/queryPerDayAmount", function (r) {
    $("#payment_div").highcharts('StockChart', {
      credits: {
        text: 'EasyAcc',
        href: ''
      },
      title: {
        text: '每日支付请款单总额'
      },
      rangeSelector: {
        enabled: true,
        selected: 2
      },
      xAxis: {
        title: {
          text: 'daily num'
        },
        type: 'datetime'
      },
      series: r['series']
    });

  });

});