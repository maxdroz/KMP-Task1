Команда для получения контента заголовков новостной ленты - adb shell content query --uri content://com.opera.browser.newsfeed/newsfeed --projection title

Пример для вставки контента создать не получилось, уж слишком много оно требует параметров для вставки, но зато получилось привести пример изменения и удаления контента

Изменение - adb shell content update --uri content://com.opera.browser.newsfeed/newsfeed --bind title:s:Plain text //Заменит все заголовки на "Plain text"

Удаление - adb shell content delete --uri content://com.opera.browser.newsfeed/newsfeed //Удалит всё
