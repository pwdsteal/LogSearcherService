
<html>
<head>
    <title>Security login example</title>


</head>

<!-- Include Required Prerequisites -->
<script type="text/javascript" src="http://cdn.jsdelivr.net/jquery/1/jquery.min.js"></script>
<script type="text/javascript" src="http://cdn.jsdelivr.net/momentjs/latest/moment.min.js"></script>
<link rel="stylesheet" type="text/css" href="http://cdn.jsdelivr.net/bootstrap/3/css/bootstrap.css" />
 
<!-- Include Date Range Picker -->
<script type="text/javascript" src="http://cdn.jsdelivr.net/bootstrap.daterangepicker/2/daterangepicker.js"></script>
<link rel="stylesheet" type="text/css" href="http://cdn.jsdelivr.net/bootstrap.daterangepicker/2/daterangepicker.css" />
<body >
<div id="result">
    <input type="text" id="daterange1" name="daterange" value="Select Date Range" />
    <p><input type="button" value="Create" onclick="create()"></p>
    <p id="end">last</p>
</div>
 
<script type="text/javascript">
    function create() {
        var $div = $('input[id^="daterange"]:last');
        var $target = $("#result");
        var num = parseInt( $div.prop("id").match(/\d+/g), 10 ) +1;
        var $klon = $div.clone().prop('id', 'klon'+num );
        $target.before($klon);


        var start = moment().subtract(29, 'days');
        var end = moment();

        $klon.daterangepicker({
            startDate: start,
            endDate: end,
            timePicker: true,
            timePicker24Hour: true,
            ranges: {
                'Today': [moment(), moment()],
                'Yesterday': [moment().subtract(1, 'days'), moment().subtract(1, 'days')],
                'Last 7 Days': [moment().subtract(6, 'days'), moment()],
                'Last 30 Days': [moment().subtract(29, 'days'), moment()],
                'This Month': [moment().startOf('month'), moment().endOf('month')],
                'Last Month': [moment().subtract(1, 'month').startOf('month'), moment().subtract(1, 'month').endOf('month')]
            }
        });

    }


$(function() {

    var start = moment().subtract(29, 'days');
    var end = moment();

    $('input[name="daterange"]').daterangepicker({
        startDate: start,
        endDate: end,
        timePicker: true,
        timePicker24Hour: true,
        ranges: {
            'Today': [moment(), moment()],
            'Yesterday': [moment().subtract(1, 'days'), moment().subtract(1, 'days')],
            'Last 7 Days': [moment().subtract(6, 'days'), moment()],
            'Last 30 Days': [moment().subtract(29, 'days'), moment()],
            'This Month': [moment().startOf('month'), moment().endOf('month')],
            'Last Month': [moment().subtract(1, 'month').startOf('month'), moment().subtract(1, 'month').endOf('month')]
        }
    });
});
</script>
</body>
</html>