/*
 * This file is part of PCAPdroid.
 *
 * PCAPdroid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PCAPdroid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PCAPdroid.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2022 - Emanuele Faranda
 */

#ifndef __PCAPD_PRIV_H__
#define __PCAPD_PRIV_H__

#include <stdint.h>
#include <net/if.h>
#include <pcap.h>
#include "common/uid_lru.h"
#include "common/uid_resolver.h"
#include "common/utils.h"
#include "zdtun.h"

#define PCAPD_MAX_INTERFACES 16

typedef struct {
  char *ifnames[PCAPD_MAX_INTERFACES];
  char *bpf;
  char *log_file;
  int uid_filter;
  int num_interfaces;
  int inet_ifid;
  uint8_t dump_datalink;
  uint8_t daemonize;
  uint8_t no_client;
  uint8_t quiet;
} pcapd_conf_t;

typedef struct {
  char name[IFNAMSIZ];
  int ifidx;
  uint8_t ifid;       // positional interface index
  uint8_t is_file;
  pcap_t *pd;
  int pf;
  int dlink;
  int ipoffset;
  uint64_t mac;
  uint32_t ip;
  struct in6_addr ip6;
  time_t next_stats_update;
  struct pcap_stat stats;
} pcapd_iface_t;

typedef struct {
  char bpf[512];
  char nlbuf[8192]; /* >= 8192 to avoid truncation, see "man 7 netlink" */

  int nlsock;
  int client;

  zdtun_t *tun;
  uid_lru_t *lru;
  uid_resolver_t *resolver;
  pcapd_iface_t *inet_iface;
  pcapd_iface_t ifaces[PCAPD_MAX_INTERFACES];
  struct pcap_stat stats;
  fd_set sel_fds;
  int maxfd;
  pcapd_conf_t *conf;
} pcapd_runtime_t;

void init_conf(pcapd_conf_t *conf);
int run_pcap_dump(pcapd_conf_t *conf);

#endif
