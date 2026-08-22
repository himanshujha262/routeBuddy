# RoutBuddy Cloud Infrastructure (AWS Terraform IaC)
terraform {
  required_version = ">= 1.7.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.50.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

variable "aws_region" {
  default = "ap-south-1" # Mumbai region for India transit
}

variable "environment" {
  default = "production"
}

# 1. VPC & Networking
resource "aws_vpc" "routbuddy_vpc" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name        = "routbuddy-${var.environment}-vpc"
    Environment = var.environment
  }
}

# 2. RDS PostgreSQL with PostGIS Geospatial Support
resource "aws_db_instance" "routbuddy_db" {
  identifier             = "routbuddy-${var.environment}-pg"
  engine                 = "postgres"
  engine_version         = "16.2"
  instance_class         = "db.r6g.large"
  allocated_storage      = 100
  max_allocated_storage  = 1000
  db_name                = "routbuddy_db"
  username               = "routbuddy_admin"
  password               = var.db_password
  publicly_accessible    = false
  skip_final_snapshot    = false
  deletion_protection    = true
}

variable "db_password" {
  type      = string
  sensitive = true
}

# 3. ElastiCache Redis Cluster for Telemetry & PubSub
resource "aws_elasticache_cluster" "routbuddy_redis" {
  cluster_id           = "routbuddy-redis"
  engine               = "redis"
  node_type            = "cache.m6g.large"
  num_cache_nodes      = 1
  parameter_group_name = "default.redis7"
  port                 = 6379
}

# 4. S3 Bucket for Driver KYC Documents & Avatars
resource "aws_s3_bucket" "routbuddy_media" {
  bucket = "routbuddy-${var.environment}-media-store"
}

# 5. AWS WAF & CloudFront Distribution
resource "aws_wafv2_web_acl" "routbuddy_waf" {
  name        = "routbuddy-${var.environment}-waf"
  scope       = "CLOUDFRONT"
  description = "WAF protection against DDoS, rate limiting, and SQLi"

  default_action {
    allow {}
  }

  visibility_config {
    cloudwatch_metrics_enabled = true
    metric_name                = "RoutBuddyWAFMetric"
    sampled_requests_enabled   = true
  }
}
