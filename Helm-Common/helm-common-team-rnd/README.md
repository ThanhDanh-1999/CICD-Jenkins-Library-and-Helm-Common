helm registry login docker.io -u username -p password 
helm package ./helm-common-team-rnd
helm push helm-common-team-rnd-0.1.0.tgz oci://docker.io/danhnt

Update use apiVersion: networking.k8s.io/v1
helm registry login docker.io -u username -p password 
helm package ./helm-common-team-rnd
helm push helm-common-team-rnd-1.0.0.tgz oci://docker.io/danhnt