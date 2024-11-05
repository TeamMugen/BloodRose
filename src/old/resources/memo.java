
        Map<String, Object> maap = new HashMap<String, Object>() {
            {
                put("ai", 10);
                put("uo", "a");
            }
        };



        getMessage("spawn_args_not_enough2")
                .replace("%player%", sender.getName())
                .replace("%prefix%", prefix)