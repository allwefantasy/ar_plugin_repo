for plugin in "user-system" "app_runtime_with_db" "ar_runtime_web_console"
do
sfcli plugin --add ${plugin}:1.0.0 --token admin
done
